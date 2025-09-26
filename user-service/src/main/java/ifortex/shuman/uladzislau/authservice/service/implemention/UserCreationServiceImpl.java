package ifortex.shuman.uladzislau.authservice.service.implemention;

import ifortex.shuman.uladzislau.authservice.annotation.logging.UserLogging;
import ifortex.shuman.uladzislau.authservice.dto.CreateUserByAdminRequestDto;
import ifortex.shuman.uladzislau.authservice.dto.CreateUserResponseDto;
import ifortex.shuman.uladzislau.authservice.model.Role;
import ifortex.shuman.uladzislau.authservice.model.User;
import ifortex.shuman.uladzislau.authservice.model.UserRole;
import ifortex.shuman.uladzislau.authservice.model.UserStatus;
import ifortex.shuman.uladzislau.authservice.service.*;
import ifortex.shuman.uladzislau.authservice.service.validation.UserValidator;
import ifortex.shuman.uladzislau.authservice.util.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCreationServiceImpl implements UserCreationService {

  private final UserValidator userValidator;
  private final UserService userService;
  private final RoleService roleService;
  private final PasswordEncoder passwordEncoder;
  private final NotificationService notificationService;
  private final UserMapper userMapper;

  @Value("${application.security.temp-password.expiration-hours}")
  private long tempPasswordExpirationHours;

  @Override
  @Transactional // saving and notification
  @UserLogging
  public CreateUserResponseDto createUser(CreateUserByAdminRequestDto request, UserRole role) {
    log.info("Attempting to create a new user with role {}", role);
    userValidator.validateEmailIsAvailable(request.getEmail());

    String tempPassword = userService.generateRandomPassword();
    User user = buildNewUser(request, role, tempPassword);
    userService.save(user);
    log.info("Successfully created a new user with email {} and role {}", request.getEmail(), role);

    notificationService.sendTemporaryPasswordEmail(user.getEmail(), tempPassword);
    return CreateUserResponseDto.builder()
        .user(userMapper.toUserDto(user))
        .temporaryPassword(tempPassword)
        .build();
  }

  private User buildNewUser(CreateUserByAdminRequestDto request, UserRole role,
      String tempPassword) {
    Role userRole = roleService.findByName(role);
    return User.builder()
        .email(request.getEmail())
        .password(passwordEncoder.encode(tempPassword))
        .fullName(request.getFullName())
        .phone(request.getPhone())
        .role(userRole)
        .status(UserStatus.ACTIVE)
        .is2FAEnabled(false)
        .isPasswordTemporary(true)
        .passwordExpiryTime(Instant.now().plus(tempPasswordExpirationHours, ChronoUnit.HOURS))
        .build();
  }
}