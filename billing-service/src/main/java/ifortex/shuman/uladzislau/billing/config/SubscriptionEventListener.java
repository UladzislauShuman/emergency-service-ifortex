package ifortex.shuman.uladzislau.billing.config;

import ifortex.shuman.uladzislau.billing.config.properties.StripePlanMappingProperties;
import ifortex.shuman.uladzislau.billing.model.Plan;
import ifortex.shuman.uladzislau.billing.model.Subscription;
import ifortex.shuman.uladzislau.billing.repository.PlanRepository;
import ifortex.shuman.uladzislau.billing.repository.SubscriptionRepository;
import ifortex.shuman.uladzislau.billing.dto.SubscriptionChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionEventListener {

  private final PlanRepository planRepository;
  private final SubscriptionRepository subscriptionRepository;
  private final StripePlanMappingProperties planMapping;

  @EventListener
  public void handleSubscriptionChanged(SubscriptionChangedEvent event) {
    log.info("Received local SubscriptionChangedEvent: {}", event);

    switch (event.status()) {
      case "CANCELED" -> cancelSubscription(event);
      case "ACTIVE" -> createSubscription(event);
    }
  }

  private void cancelSubscription(SubscriptionChangedEvent event) {
    subscriptionRepository.findActiveSubscriptionByUserId(event.userId())
        .ifPresentOrElse(sub -> {
          sub.setStatus("CANCELED");
          sub.setEndsAt(event.endsAt());
          sub.setCanceledAt(Instant.now());
          subscriptionRepository.save(sub);
          log.info("Successfully canceled active subscription for user {}", event.userId());
        }, () -> log.warn("No active subscription found to cancel for user {}", event.userId()));
  }

  private void createSubscription(SubscriptionChangedEvent event) {
    Optional.ofNullable(planMapping.getPlanMapping().get(event.planCode()))
        .flatMap(planRepository::findByPlanCode)
        .ifPresentOrElse(plan -> {
          subscriptionRepository.findActiveSubscriptionByUserId(event.userId())
              .ifPresent(existingSub -> {
                existingSub.setStatus("SUPERSEDED");
                existingSub.setCanceledAt(Instant.now());
                subscriptionRepository.save(existingSub);
                log.info("Deactivated old subscription {} for user {}", existingSub.getId(), event.userId());
              });

          Instant endsAt = calculateEndsAt(event.startsAt(), plan);

          subscriptionRepository.save(Subscription.builder()
              .userId(event.userId())
              .plan(plan)
              .status("ACTIVE")
              .startsAt(event.startsAt())
              .endsAt(endsAt)
              .build());

          log.info("Created new subscription for user {} with plan '{}', ends at {}", event.userId(), plan.getName(), endsAt);
        }, () -> log.error("No planCode mapping found for stripePriceId {}. Cannot process event.", event.planCode()));
  }

  private Instant calculateEndsAt(Instant startsAt, Plan plan) {
    return startsAt.atZone(ZoneId.systemDefault())
        .plus(plan.getIntervalCount(), plan.getIntervalUnit())
        .toInstant();
  }
}