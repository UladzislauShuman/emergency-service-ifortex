package ifortex.shuman.uladzislau.authservice.service.implemention;

import ifortex.shuman.uladzislau.authservice.dto.ChangePasswordRequestDto;
import ifortex.shuman.uladzislau.authservice.dto.EmailChangeConfirmDto;
import ifortex.shuman.uladzislau.authservice.dto.EmailChangeRequestDto;
import ifortex.shuman.uladzislau.authservice.dto.EmailChangeResponseDto;
import ifortex.shuman.uladzislau.authservice.dto.MessageResponseDto;
import ifortex.shuman.uladzislau.authservice.dto.UpdateProfileRequestDto;
import ifortex.shuman.uladzislau.authservice.dto.UserDto;
import ifortex.shuman.uladzislau.authservice.exception.ResourceConflictException;
import ifortex.shuman.uladzislau.authservice.exception.UserAccountLockedException;
import ifortex.shuman.uladzislau.authservice.model.TokenType;
import ifortex.shuman.uladzislau.authservice.model.User;
import ifortex.shuman.uladzislau.authservice.model.UserStatus;
import ifortex.shuman.uladzislau.authservice.service.NotificationService;
import ifortex.shuman.uladzislau.authservice.service.OtpService;
import ifortex.shuman.uladzislau.authservice.service.ProfileUserService;
import ifortex.shuman.uladzislau.authservice.service.UserService;
import ifortex.shuman.uladzislau.authservice.service.UserTokenService;
import ifortex.shuman.uladzislau.authservice.service.validation.UserValidator;
import ifortex.shuman.uladzislau.authservice.util.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileUserServiceImpl implements ProfileUserService {

  private final UserTokenService userTokenService;
  private final NotificationService notificationService;
  private final UserService userService;
  private final UserMapper userMapper;
  private final UserValidator userValidator;
  private final OtpService otpService;

  @Override
  public UserDto updateProfile(Long userId, UpdateProfileRequestDto request) {
    User userToUpdate = userService.findById(userId);
    updateUserFields(userToUpdate, request);
    User updatedUser = userService.save(userToUpdate);
    log.info("User {} updated their profile.", userToUpdate.getId());
    return userMapper.toUserDto(updatedUser);
  }

  @Override
  public MessageResponseDto changePassword(User currentUser, ChangePasswordRequestDto request) {
    User user = getAndValidateCurrentUser(currentUser);
    userValidator.checkPasswordMatch(user, request.getCurrentPassword());
    userValidator.validatePasswordConfirmation(request.getNewPassword(),
        request.getConfirmationPassword());
    userValidator.ensureNewPasswordIsDifferent(user, request.getNewPassword());
    userService.updateUserPassword(user.getId(), request.getNewPassword());
    log.info("User {} changed their password successfully.", user.getId());
    return new MessageResponseDto("Password changed successfully.");
  }

  @Override
  public MessageResponseDto setTwoFactorAuthentication(User currentUser, boolean enable) {
    User user = getAndValidateCurrentUser(currentUser);
    user.set2FAEnabled(enable);
    userService.save(user);
    log.info("User {} {} 2FA.", user.getId(), enable ? "enabled" : "disabled");
    String message = String.format("Two-factor authentication has been %s.",
        enable ? "enabled" : "disabled");
    return new MessageResponseDto(message);
  }

  @Override
  public MessageResponseDto requestEmailChange(User currentUser, EmailChangeRequestDto request) {
    User user = getAndValidateCurrentUser(currentUser);
    userValidator.checkPasswordMatch(user, request.getCurrentPassword());
    userValidator.validateEmailIsAvailable(request.getNewEmail());
    notificationService.sendEmailChangeNotificationToOldEmail(user.getEmail(),
        request.getNewEmail());
    otpService.generateAndSendOtpForEmailChange(user.getId(), request.getNewEmail());
    log.info("User {} requested to change email to {}.", user.getId(), request.getNewEmail());
    return new MessageResponseDto("A verification code has been sent to your new email address.");
  }

  @Override
  @Transactional
  public EmailChangeResponseDto confirmEmailChange(User currentUser,
      EmailChangeConfirmDto request) {
    User user = getAndValidateCurrentUser(currentUser);
    String newEmail = otpService.validateAndRetrieveNewEmailFromOtp(user.getId(),
        request.getOtpCode());

    userService.updateUserEmailAndInvalidateTokens(user.getId(), newEmail);

    userTokenService.deleteAllByUserAndType(user, TokenType.REFRESH);
    log.info("User {} successfully changed their email to {}.", user.getId(), newEmail);

    return EmailChangeResponseDto.builder()
        .message("Email changed successfully. Please log in again with your new email.")
        .reLoginRequired(true)
        .build();
  }

  @Override
  public MessageResponseDto linkGoogleAccount(Long userId, String googleId, String googleEmail) {
    User user = userService.findById(userId);
    if (!user.getEmail().equals(googleEmail)) {
      log.warn(
          "User {} trying to link a Google account with a mismatched email (Account: {}, Google: {}).",
          user.getId(), user.getEmail(), googleEmail);
      throw new IllegalArgumentException("Google account email does not match your profile email.");
    }
    user.setGoogleId(googleId);
    userService.save(user);
    log.info("User {} linked their Google account.", userId);
    return new MessageResponseDto("Account successful was linked with Google");
  }

  @Override
  @Transactional
  public MessageResponseDto requestAccountDeletion(Long userId) {
    User user = userService.findById(userId);
    if (user.getStatus() != UserStatus.ACTIVE) {
      throw new ResourceConflictException(
          "Account deletion can only be requested for active accounts.");
    }

    user.setStatus(UserStatus.PENDING_DELETION);
    userTokenService.deleteAllByUserAndType(user, TokenType.REFRESH);

    userService.save(user);
    log.info("User {} requested account deletion. Status set to PENDING_DELETION.", user.getId());
    return new MessageResponseDto(
        "Your account deletion request has been submitted and will be reviewed by an administrator.");
  }

  @Override
  public UserDto getUserProfile(Long userId) {
    User user = userService.findById(userId);
    return userMapper.toUserDto(user);
  }

  private void updateUserFields(User user, UpdateProfileRequestDto request) {
    if (request.getFullName() != null) {
      user.setFullName(request.getFullName());
    }
    if (request.getPhone() != null) {
      user.setPhone(request.getPhone());
    }
  }

  private User getAndValidateCurrentUser(User currentUser) {
    User freshUser = userService.findById(currentUser.getId());
    if (freshUser.getStatus() != UserStatus.ACTIVE) {
      String message = switch (freshUser.getStatus()) {
        case DELETED -> "Your account has been deleted.";
        case PENDING_VERIFICATION -> "Your account is pending email verification.";
        case PASSWORD_RESET_PENDING -> "Your account is locked pending a password reset.";
        default -> "Your account is currently inactive and cannot perform this action.";
      };
      throw new UserAccountLockedException(message);
    }
    return freshUser;
  }
}
