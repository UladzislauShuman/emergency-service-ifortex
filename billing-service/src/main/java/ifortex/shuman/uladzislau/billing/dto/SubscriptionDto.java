package ifortex.shuman.uladzislau.billing.dto;

import java.time.Instant;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubscriptionDto {
    private String planName;
    private String status;
    private Instant endsAt;
}