package ifortex.shuman.uladzislau.billing.dto;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class ActiveSubscriptionDto {
    private String planCode;
    private String status;
    private Instant endsAt;
}