package ifortex.shuman.uladzislau.billing.controller;

import ifortex.shuman.uladzislau.billing.dto.AccountDetailsDto;
import ifortex.shuman.uladzislau.billing.dto.SubscribeRequestDto;
import ifortex.shuman.uladzislau.billing.dto.SubscribeResponseDto;
import ifortex.shuman.uladzislau.billing.service.BillingService;
import ifortex.shuman.uladzislau.billing.service.SubscriptionDetailsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
public class SubscriptionApiController {

  private final BillingService billingService;
  private final SubscriptionDetailsService accountDetailsService;

  @GetMapping("/details")
  public ResponseEntity<AccountDetailsDto> getAccountDetails(
      @RequestHeader("X-User-Id") Long userId) {
    return ResponseEntity.ok(accountDetailsService.getAccountDetailsForUser(userId));
  }

  @PostMapping("/checkout-sessions")
  public ResponseEntity<SubscribeResponseDto> createSubscriptionCheckoutSession(
      @RequestHeader("X-User-Id") Long userId,
      @Valid @RequestBody SubscribeRequestDto request) {
    return ResponseEntity.ok(
        billingService.createSubscriptionCheckoutSession(userId, request.getPriceId()));
  }

  @PostMapping("/portal-sessions")
  public ResponseEntity<SubscribeResponseDto> createCustomerPortalSession(
      @RequestHeader("X-User-Id") Long userId) {
    return ResponseEntity.ok(SubscribeResponseDto.builder()
        .redirectUrl(billingService.createCustomerPortalSession(userId)).build());
  }
}