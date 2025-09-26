package ifortex.shuman.uladzislau.billing.controller;

import ifortex.shuman.uladzislau.billing.dto.MessageResponseDto;
import ifortex.shuman.uladzislau.billing.service.BillingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stripe-webhook")
@RequiredArgsConstructor
public class StripeWebhookController {

  private final BillingService billingService;

  private static final String STRIPE_SIGNATURE_HEADER = "Stripe-Signature";

  @PostMapping
  public ResponseEntity<MessageResponseDto> handleStripeEvent(
      @RequestBody String payload,
      @RequestHeader(STRIPE_SIGNATURE_HEADER) String sigHeader) {
    return ResponseEntity.ok(billingService.handleWebhookEvent(payload, sigHeader));
  }
}