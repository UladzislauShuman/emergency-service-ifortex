package ifortex.shuman.uladzislau.authservice.client;

import ifortex.shuman.uladzislau.authservice.dto.ActiveSubscriptionDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Map;

@FeignClient(name = "billing-service", url = "${application.services.billing-service.url}")
public interface BillingServiceClient {

    @GetMapping("/api/internal/subscriptions/user/{userId}/active")
    ActiveSubscriptionDto getActiveSubscription(@PathVariable("userId") Long userId);
}