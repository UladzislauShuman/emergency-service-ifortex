package ifortex.shuman.uladzislau.billing.controller;

import ifortex.shuman.uladzislau.billing.dto.ActiveSubscriptionDto;
import ifortex.shuman.uladzislau.billing.repository.SubscriptionRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/subscriptions")
@RequiredArgsConstructor
public class InternalController {

  private final SubscriptionRepository subscriptionRepository;

  @GetMapping("/user/{userId}/active")
  public ResponseEntity<ActiveSubscriptionDto> getActiveSubscription(@PathVariable Long userId) {
    Optional<ActiveSubscriptionDto> activeSubDto = subscriptionRepository
        .findActiveSubscriptionByUserId(userId)
        .map(sub -> ActiveSubscriptionDto.builder()
            .planCode(sub.getPlan().getPlanCode())
            .status(sub.getStatus())
            .endsAt(sub.getEndsAt())
            .build());

    return activeSubDto.map(ResponseEntity::ok)
        .orElse(ResponseEntity.ok().build());
  }
}