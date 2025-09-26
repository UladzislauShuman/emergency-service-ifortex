package ifortex.shuman.uladzislau.billing.dto;
import java.time.Instant;

public record SubscriptionChangedEvent(
    Long userId,
    String stripeSubscriptionId,
    String planCode,
    String status,
    Instant startsAt,
    Instant endsAt
) {}