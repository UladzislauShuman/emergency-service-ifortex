package ifortex.shuman.uladzislau.billing.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountDetailsDto {
    private UserDto user;
    private SubscriptionDto activeSubscription;
    private List<PlanDto> availablePlans;
}