package ifortex.shuman.uladzislau.authservice.dto;

import ifortex.shuman.uladzislau.authservice.model.UserRole;
import ifortex.shuman.uladzislau.authservice.model.UserStatus;
import lombok.Value;

@Value
public class UserAuthorizationSnapshot {
    Long userId;
    UserRole role;
    boolean isSubscriptionActive;
    UserStatus userStatus;
}