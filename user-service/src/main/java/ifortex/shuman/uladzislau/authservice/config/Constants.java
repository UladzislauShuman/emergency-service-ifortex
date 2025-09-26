package ifortex.shuman.uladzislau.authservice.config;

import ifortex.shuman.uladzislau.authservice.model.UserStatus;

import java.util.List;

public final class Constants {

  public static final List<UserStatus> VALID_STATUSES_FOR_LOGIN =
      List.of(UserStatus.ACTIVE, UserStatus.PASSWORD_RESET_PENDING,
          UserStatus.PENDING_DELETION);
}
