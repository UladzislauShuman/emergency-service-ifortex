package ifortex.shuman.uladzislau.authservice.util.mapper;

import ifortex.shuman.uladzislau.authservice.dto.UserDto;
import ifortex.shuman.uladzislau.authservice.model.User;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

  public UserDto toUserDto(User user) {
    if (Objects.isNull(user)) {
      return null;
    }

    return UserDto.builder()
        .id(user.getId())
        .email(user.getEmail())
        .fullName(user.getFullName())
        .phone(user.getPhone())
        .status(user.getStatus())
        .is2FAEnabled(user.is2FAEnabled())
        .hasGoogleAccount(user.getGoogleId() != null)
        .role(user.getRole() != null ? user.getRole().getName() : null)
        .isPermanentlyBlocked(user.isPermanentlyBlocked())
        .blockedUntil(user.getBlockedUntil())
        .build();
  }
}