package ifortex.shuman.uladzislau.authservice.service.permission;

import ifortex.shuman.uladzislau.authservice.client.BillingServiceClient;
import ifortex.shuman.uladzislau.authservice.dto.ActiveSubscriptionDto;
import ifortex.shuman.uladzislau.authservice.dto.UserAuthorizationSnapshot;
import ifortex.shuman.uladzislau.authservice.model.Permissions;
import ifortex.shuman.uladzislau.authservice.model.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionPermissionContributor implements PermissionContributor {

  private final BillingServiceClient billingServiceClient;

  @Override
  public Set<String> contributePermissions(UserAuthorizationSnapshot snapshot) {
    if (snapshot.getRole() != UserRole.ROLE_CLIENT) {
      return Collections.emptySet();
    }

    try {
      ActiveSubscriptionDto activeSubscription = billingServiceClient.getActiveSubscription(snapshot.getUserId());

      if (activeSubscription != null && "ACTIVE".equals(activeSubscription.getStatus())) {
        log.debug("User {} has active subscription with plan {}. Granting MANAGE permission.",
            snapshot.getUserId(), activeSubscription.getPlanCode());
        return Set.of(Permissions.SUBSCRIPTION_MANAGE);
      } else {
        log.debug("User {} has no active subscription. Granting CREATE permission.", snapshot.getUserId());
        return Set.of(Permissions.SUBSCRIPTION_CREATE);
      }
    } catch (Exception e) {
      log.error("Failed to fetch subscription status for user {}. Granting CREATE permission as a fallback. Error: {}",
          snapshot.getUserId(), e.getMessage());
      return Set.of(Permissions.SUBSCRIPTION_CREATE);
    }
  }
}