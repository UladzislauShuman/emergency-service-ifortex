package ifortex.shuman.uladzislau.billing.service.implemention;

import ifortex.shuman.uladzislau.billing.client.UserServiceClient;
import ifortex.shuman.uladzislau.billing.dto.AccountDetailsDto;
import ifortex.shuman.uladzislau.billing.dto.PlanDto;
import ifortex.shuman.uladzislau.billing.dto.SubscriptionDto;
import ifortex.shuman.uladzislau.billing.dto.UserDto;
import ifortex.shuman.uladzislau.billing.repository.SubscriptionRepository;
import ifortex.shuman.uladzislau.billing.service.BillingService;
import ifortex.shuman.uladzislau.billing.service.SubscriptionDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionDetailsServiceImpl implements SubscriptionDetailsService {

    private final BillingService billingService;
    private final SubscriptionRepository subscriptionRepository;
    private final UserServiceClient userServiceClient;

    @Override
    public AccountDetailsDto getAccountDetailsForUser(Long userId) {
        UserDto userDto = userServiceClient.getUserById(userId);

        SubscriptionDto activeSubscription = subscriptionRepository.findActiveSubscriptionByUserId(userId)
            .map(sub -> SubscriptionDto.builder()
                .planName(sub.getPlan().getName())
                .status(sub.getStatus())
                .endsAt(sub.getEndsAt())
                .build())
            .orElse(null);

        List<PlanDto> availablePlans = billingService.getAvailablePlans();

        return AccountDetailsDto.builder()
            .user(userDto)
            .activeSubscription(activeSubscription)
            .availablePlans(availablePlans)
            .build();
    }
}