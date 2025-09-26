package ifortex.shuman.uladzislau.authservice.service.implemention;

import ifortex.shuman.uladzislau.authservice.annotation.logging.UserLogging;
import ifortex.shuman.uladzislau.authservice.config.properties.FrontendProperties;
import ifortex.shuman.uladzislau.authservice.dto.AdminPasswordResetResponseDto;
import ifortex.shuman.uladzislau.authservice.dto.BlockUserRequestDto;
import ifortex.shuman.uladzislau.authservice.dto.MessageResponseDto;
import ifortex.shuman.uladzislau.authservice.dto.UpdateUserByAdminRequestDto;
import ifortex.shuman.uladzislau.authservice.dto.UserDto;
import ifortex.shuman.uladzislau.authservice.dto.UserSearchRequestDto;
import ifortex.shuman.uladzislau.authservice.exception.ResourceConflictException;
import ifortex.shuman.uladzislau.authservice.model.TokenType;
import ifortex.shuman.uladzislau.authservice.model.User;
import ifortex.shuman.uladzislau.authservice.model.UserSearchStatus;
import ifortex.shuman.uladzislau.authservice.model.UserStatus;
import ifortex.shuman.uladzislau.authservice.repository.UserRepository;
import ifortex.shuman.uladzislau.authservice.repository.specification.UserSpecification;
import ifortex.shuman.uladzislau.authservice.service.AdminUserService;
import ifortex.shuman.uladzislau.authservice.service.NotificationService;
import ifortex.shuman.uladzislau.authservice.service.UserService;
import ifortex.shuman.uladzislau.authservice.service.UserTokenService;
import ifortex.shuman.uladzislau.authservice.service.validation.UserValidator;
import ifortex.shuman.uladzislau.authservice.util.mapper.UserMapper;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserServiceImpl implements AdminUserService {

  private final UserMapper userMapper;
  private final UserRepository userRepository;
  private final UserSpecification userSpecification;
  private final UserService userService;
  private final UserTokenService userTokenService;
  private final NotificationService notificationService;
  private final UserValidator userValidator;
  private final FrontendProperties frontendProperties;

  @Value("${application.security.temp-password.expiration-hours}")
  private long tempPasswordExpirationHours;

  @Override
  @UserLogging
  public UserDto updateUserByAdmin(Long userId, UpdateUserByAdminRequestDto request) {
    User user = userService.findById(userId);
    userValidator.ensureNotSuperAdmin(user);
    updateUserData(user, request);
    User updatedUser = userService.save(user);
    log.info("Admin updated profile for user ID: {}", userId);
    return userMapper.toUserDto(updatedUser);
  }

  @Override
  @UserLogging
  @Transactional
  public UserDto updateBlockStatus(User whoBlocking, Long userToBlockId,
      BlockUserRequestDto request) {
    User admin = userService.findById(whoBlocking.getId());
    userValidator.ensureIsNotBlockingOrDeleteHimSelf(admin, userToBlockId);

    User userToUpdate = userService.findById(userToBlockId);
    userValidator.canBlockOrDeleteUser(admin, userToUpdate);

    if (request.isPermanent()) {
      userToUpdate.setPermanentlyBlocked(true);
      userToUpdate.setBlockedUntil(null);
      log.info("Admin {} permanently blocked user {}", admin.getEmail(), userToUpdate.getEmail());
    } else {
      userToUpdate.setPermanentlyBlocked(false);
      userToUpdate.setBlockedUntil(request.getBlockedUntil());
      if (request.getBlockedUntil() != null) {
        log.info("Admin {} blocked user {} until {}", admin.getEmail(), userToUpdate.getEmail(),
            request.getBlockedUntil());
      } else {
        log.info("Admin {} unblocked user {}", admin.getEmail(), userToUpdate.getEmail());
      }
    }

    if (userToUpdate.isPermanentlyBlocked() || (userToUpdate.getBlockedUntil() != null
        && userToUpdate.getBlockedUntil().isAfter(Instant.now()))) {
      userTokenService.deleteAllByUserAndType(userToUpdate, TokenType.REFRESH);
      log.info("All refresh tokens for user {} have been deleted due to blocking.",
          userToUpdate.getEmail());
    }

    User updatedUser = userService.save(userToUpdate);
    return userMapper.toUserDto(updatedUser);
  }

  @Override
  @UserLogging
  public MessageResponseDto softDeleteUser(Long userId, User currentUser) {
    log.info("Admin is performing a soft delete for user ID: {}", userId);
    User userToDelete = userService.findById(userId);
    userValidator.validateDeletionPrivileges(currentUser, userToDelete);
    userService.softDeleteUser(userId);
    return new MessageResponseDto("User " + userToDelete.getEmail() + " has been soft-deleted.");
  }

  @Override
  @UserLogging
  public MessageResponseDto hardDeleteUser(Long userId, User currentUser) {
    log.info("Admin is performing a hard delete for user ID: {}", userId);
    User userToDelete = userService.findById(userId);
    userValidator.validateDeletionPrivileges(currentUser, userToDelete);
    userService.hardDeleteUser(userId);
    return new MessageResponseDto("User with ID " + userId + " has been permanently deleted.");
  }


  @Override
  public Page<UserDto> findUsersByComplexFilter(UserSearchRequestDto request, Pageable pageable) {
    log.debug(
        "Finding users with complex filter. Request: {}, Pageable: {}",
        request, pageable);
    Specification<User> spec = buildComplexSpecification(request);
    Page<User> userPage = userRepository.findAll(spec, pageable);
    log.info("Found {} users matching the criteria.", userPage.getTotalElements());
    return userPage.map(userMapper::toUserDto);
  }

  @Override
  @Transactional
  @UserLogging
  public AdminPasswordResetResponseDto sendPasswordResetLink(Long userId) {
    User user = userService.findById(userId);
    userValidator.ensureNotSuperAdmin(user);

    user.setStatus(UserStatus.PASSWORD_RESET_PENDING);
    userService.save(user);

    String token = userTokenService.createPasswordResetToken(user);
    String resetUrl = frontendProperties.getBaseUrl()
        + frontendProperties.getAdminResetPasswordPath()
        + "?token=" + token;

    notificationService.sendPasswordResetEmail(user.getEmail(), resetUrl);
    log.info("Admin initiated password reset by link for user {}", userId);

    return AdminPasswordResetResponseDto.builder()
        .message("Password reset link has been sent to " + user.getEmail())
        .build();
  }

  @Override
  @Transactional
  @UserLogging
  public AdminPasswordResetResponseDto generateTemporaryPassword(Long userId) {
    String tempPassword = userService.setUserTemporaryPassword(userId);
    log.info("Admin generated a temporary password for user {}", userId);

    return AdminPasswordResetResponseDto.builder()
        .message("Temporary password generated. Please provide it to the user securely.")
        .temporaryPassword(tempPassword)
        .build();
  }

  @Override
  @UserLogging
  @Transactional
  public UserDto cancelAccountDeletion(Long userId) {
    User user = userService.findById(userId);

    if (user.getStatus() != UserStatus.PENDING_DELETION) {
      throw new ResourceConflictException(
          "User is not pending deletion. Current status: " + user.getStatus());
    }

    user.setStatus(UserStatus.ACTIVE);
    User savedUser = userService.save(user);

    log.info("Admin canceled account deletion request for user {}. Status set back to ACTIVE.",
        user.getEmail());

    return userMapper.toUserDto(savedUser);
  }

  private void updateUserData(User user, UpdateUserByAdminRequestDto request) {
    if (request.getFullName() != null) {
      user.setFullName(request.getFullName());
    }
    if (request.getPhone() != null) {
      user.setPhone(request.getPhone());
    }
  }

  private Specification<User> buildComplexSpecification(UserSearchRequestDto request) {
    Specification<User> spec = Specification.where(null);

    if (request.getRoles() != null && !request.getRoles().isEmpty()) {
      spec = spec.and(userSpecification.hasRoles(request.getRoles()));
    }

    if (request.getStatuses() != null && !request.getStatuses().isEmpty()) {
      Set<UserSearchStatus> searchStatuses = request.getStatuses();

      Specification<User> statusOrBlockSpec = Specification.where(null);
      boolean criteriaAdded = false;

      Set<UserStatus> dbStatuses = searchStatuses.stream()
          .filter(s -> s != UserSearchStatus.BLOCKED && s != UserSearchStatus.ACTIVE)
          .map(s -> UserStatus.valueOf(s.name()))
          .collect(Collectors.toSet());

      if (!dbStatuses.isEmpty()) {
        statusOrBlockSpec = statusOrBlockSpec.or(userSpecification.hasStatuses(dbStatuses));
        criteriaAdded = true;
      }

      if (searchStatuses.contains(UserSearchStatus.BLOCKED)) {
        statusOrBlockSpec = statusOrBlockSpec.or(userSpecification.isLocked());
        criteriaAdded = true;
      }

      if (searchStatuses.contains(UserSearchStatus.ACTIVE)) {
        Specification<User> activeAndUnlockedSpec = Specification
            .where(userSpecification.hasStatuses(Set.of(UserStatus.ACTIVE)))
            .and(userSpecification.isUnlocked());
        statusOrBlockSpec = statusOrBlockSpec.or(activeAndUnlockedSpec);
        criteriaAdded = true;
      }

      if (criteriaAdded) {
        spec = spec.and(statusOrBlockSpec);
      }
    }

    spec = spec.and(userSpecification.hasFullName(request.getFullName()));
    spec = spec.and(userSpecification.hasEmail(request.getEmail()));
    spec = spec.and(userSpecification.hasPhone(request.getPhone()));

    return spec.and(userSpecification.fetchRoles());
  }
}
