package ifortex.shuman.uladzislau.billing.service.implemention;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Event;
import com.stripe.model.Price;
import com.stripe.model.Product;
import com.stripe.model.StripeObject;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PriceListParams;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionCreateParams.Mode;
import ifortex.shuman.uladzislau.billing.client.UserServiceClient;
import ifortex.shuman.uladzislau.billing.config.properties.FrontendStripeProperties;
import ifortex.shuman.uladzislau.billing.config.properties.StripeProperties;
import ifortex.shuman.uladzislau.billing.dto.*;
import ifortex.shuman.uladzislau.billing.exception.BillingException;
import ifortex.shuman.uladzislau.billing.exception.EntityNotFoundException;
import ifortex.shuman.uladzislau.billing.exception.OperationForbiddenException;
import ifortex.shuman.uladzislau.billing.exception.ResourceConflictException;
import ifortex.shuman.uladzislau.billing.model.BillingProfile;
import ifortex.shuman.uladzislau.billing.repository.BillingProfileRepository;
import ifortex.shuman.uladzislau.billing.repository.SubscriptionRepository;
import ifortex.shuman.uladzislau.billing.service.BillingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeBillingService implements BillingService {

  public static final String STATUS_CANCELED = "CANCELED";
  public static final String STATUS_ACTIVE = "ACTIVE";
  public static final String EVENT_CHECKOUT_SESSION_COMPLETED = "checkout.session.completed";
  public static final String EVENT_CUSTOMER_SUBSCRIPTION_UPDATED = "customer.subscription.updated";
  public static final String EVENT_CUSTOMER_SUBSCRIPTION_DELETED = "customer.subscription.deleted";

  private final FrontendStripeProperties frontendStripeProperties;
  private final BillingProfileRepository billingProfileRepository;
  private final StripeProperties stripeProperties;
  private final ApplicationEventPublisher eventPublisher;
  private final SubscriptionRepository subscriptionRepository;
  private final UserServiceClient userServiceClient;

  @Override
  public SubscribeResponseDto createSubscriptionCheckoutSession(Long userId, String priceId) {
    UserDto user = userServiceClient.getUserById(userId);
    log.info("Creating subscription checkout session for user {}", user.getEmail());

    if (subscriptionRepository.findActiveSubscriptionByUserId(user.getId()).isPresent()) {
      throw new ResourceConflictException("User already has an active subscription.");
    }

    return executeStripeOperation(() -> {
      String customerId = getOrCreateStripeCustomer(user);
      Session session = createStripeCheckoutSession(user, priceId, customerId);
      return buildSubscribeResponse(session);
    }, user, "Could not create payment session.");
  }

  @Override
  public List<PlanDto> getAvailablePlans() {
    log.debug("Fetching available plans from Stripe");
    return executeStripeOperation(this::fetchPlansFromStripe, null,
        "Could not fetch available plans.");
  }

  @Override
  public Optional<BillingProfile> findBillingProfileByUserId(Long userId) {
    return billingProfileRepository.findByUserId(userId);
  }

  @Override
  public String createCustomerPortalSession(Long userId) {
    UserDto user = userServiceClient.getUserById(userId);
    log.info("Start creating customer portal session for user {}", user.getEmail());
    BillingProfile profile = getExistingBillingProfile(userId);
    subscriptionRepository.findActiveSubscriptionByUserId(user.getId())
        .orElseThrow(() -> new OperationForbiddenException(
            "User does not have an active subscription to manage."));
    return executeStripeOperation(
        () -> createPortalSessionUrl(buildPortalSessionParams(profile)),
        user,
        "Could not create customer portal session."
    );
  }

  @Override
  @Transactional
  public MessageResponseDto handleWebhookEvent(String payload, String sigHeader) {
    Event event = constructStripeEvent(payload, sigHeader);
    StripeObject stripeObject = extractStripeObject(event);

    routeEvent(event.getType(), stripeObject);
    return new MessageResponseDto("Stripe webhook was caught successful");
  }

  private SessionCreateParams buildSubscriptionSessionParams(UserDto user, String priceId,
      String customerId) {
    return SessionCreateParams.builder()
        .setMode(Mode.SUBSCRIPTION)
        .setCustomer(customerId)
        .addLineItem(SessionCreateParams.LineItem.builder()
            .setPrice(priceId)
            .setQuantity(1L)
            .build())
        .setSuccessUrl(frontendStripeProperties.getSubscriptionSuccessUrl())
        .setCancelUrl(frontendStripeProperties.getSubscriptionCancelUrl())
        .putMetadata("userId", user.getId().toString())
        .putMetadata("priceId", priceId)
        .setSubscriptionData(
            SessionCreateParams.SubscriptionData.builder()
                .putMetadata("userId", user.getId().toString())
                .build()
        )
        .build();
  }

  private PlanDto convertPriceToPlan(Price price) {
    Product product = price.getProductObject();
    return new PlanDto(
        price.getId(),
        product.getName(),
        BigDecimal.valueOf(price.getUnitAmount()).divide(BigDecimal.valueOf(100)),
        price.getCurrency(),
        price.getRecurring().getInterval()
    );
  }

  private Event constructStripeEvent(String payload, String sigHeader) {
    try {
      return Webhook.constructEvent(payload, sigHeader, stripeProperties.getWebhookSecret());
    } catch (SignatureVerificationException e) {
      log.warn("Invalid Stripe webhook signature!", e);
      throw new IllegalArgumentException("Invalid signature");
    }
  }

  private void handleCheckoutCompleted(Session session) {
    log.info("Handling 'checkout.session.completed' for session ID: {}", session.getId());
    if (hasMissingMetadata(session)) {
      log.warn("Missing metadata in checkout session {}", session.getId());
      return;
    }

    executeStripeOperation(() -> {
      Subscription subscription = Subscription.retrieve(session.getSubscription());
      SubscriptionChangedEvent event = buildSubscriptionCreatedEvent(session, subscription);
      eventPublisher.publishEvent(event);
      log.info("Published SubscriptionChangedEvent for new subscription {}", subscription.getId());
      return null;
    }, null, "Failed to process checkout session.");
  }

  private SubscriptionChangedEvent buildSubscriptionCreatedEvent(Session session,
      Subscription subscription) {
    return new SubscriptionChangedEvent(
        extractUserId(session),
        subscription.getId(),
        session.getMetadata().get("priceId"),
        STATUS_ACTIVE,
        Instant.ofEpochSecond(session.getCreated()),
        null
    );
  }

  private void handleSubscriptionUpdated(Subscription stripeSubscription) {
    log.info("Handling subscription update for ID: {}", stripeSubscription.getId());

    Optional.ofNullable(stripeSubscription.getMetadata().get("userId"))
        .map(Long::parseLong)
        .ifPresent(userId -> {
          SubscriptionChangedEvent event = buildSubscriptionUpdatedEvent(userId,
              stripeSubscription);
          eventPublisher.publishEvent(event);
          log.info("Published SubscriptionChangedEvent for updated subscription {}",
              stripeSubscription.getId());
        });
  }

  private SubscriptionChangedEvent buildSubscriptionUpdatedEvent(Long userId,
      Subscription stripeSubscription) {
    String priceId = stripeSubscription.getItems().getData().get(0).getPrice().getId();
    String status = "canceled".equalsIgnoreCase(stripeSubscription.getStatus()) ? STATUS_CANCELED
        : STATUS_ACTIVE;

    Instant endsAt = null;
    if (STATUS_CANCELED.equals(status) && stripeSubscription.getCanceledAt() != null) {
      endsAt = Instant.ofEpochSecond(stripeSubscription.getCanceledAt());
    }

    return new SubscriptionChangedEvent(
        userId,
        stripeSubscription.getId(),
        priceId,
        status,
        Instant.ofEpochSecond(stripeSubscription.getCreated()),
        endsAt
    );
  }

  private String getOrCreateStripeCustomer(UserDto user) {
    return billingProfileRepository.findByUserId(user.getId())
        .map(BillingProfile::getStripeCustomerId)
        .orElseGet(() -> createAndStoreStripeCustomer(user));
  }

  private String createAndStoreStripeCustomer(UserDto user) {
    return executeStripeOperation(() -> {
      Customer customer = Customer.create(buildCustomerCreateParams(user));
      String stripeId = customer.getId();
      saveOrUpdateBillingProfile(user, stripeId);
      log.info("Created Stripe Customer ID {} for user {}", stripeId, user.getEmail());
      return stripeId;
    }, user, "Could not create Stripe customer.");
  }

  private CustomerCreateParams buildCustomerCreateParams(UserDto user) {
    return CustomerCreateParams.builder()
        .setName(user.getFullName())
        .setEmail(user.getEmail())
        .putMetadata("app_user_id", user.getId().toString())
        .build();
  }

  private void saveOrUpdateBillingProfile(UserDto user, String stripeId) {
    BillingProfile profile = billingProfileRepository.findByUserId(user.getId())
        .orElseGet(() -> createNewBillingProfile(user, stripeId));
    profile.setStripeCustomerId(stripeId);
    billingProfileRepository.save(profile);
  }

  private BillingProfile createNewBillingProfile(UserDto user, String stripeCustomerId) {
    BillingProfile profile = new BillingProfile();
    profile.setUserId(user.getId());
    profile.setStripeCustomerId(stripeCustomerId);
    return profile;
  }

  private boolean hasMissingMetadata(Session session) {
    return session.getMetadata().get("userId") == null
        || session.getSubscription() == null
        || session.getMetadata().get("priceId") == null;
  }

  private Long extractUserId(Session session) {
    return Long.parseLong(session.getMetadata().get("userId"));
  }

  private Session createStripeCheckoutSession(UserDto user, String priceId, String stripeCustomerId)
      throws StripeException {
    SessionCreateParams params = buildSubscriptionSessionParams(user, priceId, stripeCustomerId);
    return Session.create(params);
  }

  private SubscribeResponseDto buildSubscribeResponse(Session session) {
    return SubscribeResponseDto.builder()
        .redirectUrl(session.getUrl())
        .build();
  }

  private PriceListParams buildPriceListParams() {
    return PriceListParams.builder()
        .setActive(true)
        .setType(PriceListParams.Type.RECURRING)
        .addExpand("data.product")
        .build();
  }

  private List<Price> fetchStripePrices(PriceListParams params) throws StripeException {
    return Price.list(params).getData();
  }

  private boolean isActiveProduct(Price price) {
    return price.getProductObject() != null && price.getProductObject().getActive();
  }

  private StripeObject extractStripeObject(Event event) {
    return event.getDataObjectDeserializer()
        .getObject()
        .orElse(null);
  }

  private void routeEvent(String eventType, StripeObject stripeObject) {
    if (stripeObject == null) {
      log.warn("Stripe object in webhook event is null. Event type: {}", eventType);
      return;
    }
    switch (eventType) {
      case EVENT_CHECKOUT_SESSION_COMPLETED -> handleCheckoutSession(stripeObject);
      case EVENT_CUSTOMER_SUBSCRIPTION_UPDATED,
           EVENT_CUSTOMER_SUBSCRIPTION_DELETED -> handleSubscriptionChange(stripeObject);
      default -> log.debug("Unhandled event type: {}", eventType);
    }
  }

  private void handleCheckoutSession(StripeObject stripeObject) {
    if (stripeObject instanceof Session session) {
      handleCheckoutCompleted(session);
    } else {
      log.error("Invalid object type for checkout.session.completed");
    }
  }

  private void handleSubscriptionChange(StripeObject stripeObject) {
    if (stripeObject instanceof Subscription subscription) {
      handleSubscriptionUpdated(subscription);
    } else {
      log.error("Invalid object type for subscription event");
    }
  }

  private BillingProfile getExistingBillingProfile(Long userId) {
    return billingProfileRepository.findByUserId(userId)
        .orElseThrow(() -> new EntityNotFoundException("User does not have a billing profile."));
  }

  private com.stripe.param.billingportal.SessionCreateParams buildPortalSessionParams(
      BillingProfile profile) {
    return com.stripe.param.billingportal.SessionCreateParams.builder()
        .setCustomer(profile.getStripeCustomerId())
        .setReturnUrl(frontendStripeProperties.getBaseUrl() + "/account?from=stripe-portal")
        .build();
  }

  private String createPortalSessionUrl(com.stripe.param.billingportal.SessionCreateParams params)
      throws StripeException {
    com.stripe.model.billingportal.Session session = com.stripe.model.billingportal.Session.create(
        params);
    return session.getUrl();
  }

  private <T> T executeStripeOperation(Callable<T> action, UserDto user, String errorMessage) {
    try {
      return action.call();
    } catch (StripeException e) {
      log.error("Stripe error for user {}: {}", (user != null ? user.getEmail() : "N/A"),
          e.getMessage());
      throw new BillingException(errorMessage, e);
    } catch (Exception e) {
      log.error("Unexpected error during Stripe operation for user {}: {}",
          (user != null ? user.getEmail() : "N/A"), e.getMessage());
      throw new BillingException("An unexpected error occurred.", e);
    }
  }

  private List<PlanDto> fetchPlansFromStripe() throws StripeException {
    return fetchStripePrices(buildPriceListParams())
        .stream()
        .filter(this::isActiveProduct)
        .map(this::convertPriceToPlan)
        .collect(Collectors.toList());
  }
}