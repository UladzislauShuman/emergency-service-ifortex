package ifortex.shuman.uladzislau.billing.service;

import ifortex.shuman.uladzislau.billing.dto.MessageResponseDto;
import ifortex.shuman.uladzislau.billing.dto.PlanDto;
import ifortex.shuman.uladzislau.billing.dto.SubscribeResponseDto;
import ifortex.shuman.uladzislau.billing.model.BillingProfile;
import java.util.List;
import java.util.Optional;

public interface BillingService {

  SubscribeResponseDto createSubscriptionCheckoutSession(Long userId, String priceId);

  List<PlanDto> getAvailablePlans();

  MessageResponseDto handleWebhookEvent(String payload, String sigHeader);

  Optional<BillingProfile> findBillingProfileByUserId(Long userId);

  String createCustomerPortalSession(Long userId);
}