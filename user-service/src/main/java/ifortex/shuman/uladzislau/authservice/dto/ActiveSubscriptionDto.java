package ifortex.shuman.uladzislau.authservice.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class ActiveSubscriptionDto {
    private String planCode;
    private String status;
    private Instant endsAt;
}