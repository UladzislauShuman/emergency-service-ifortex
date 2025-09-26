package ifortex.shuman.uladzislau.authservice.service.implemention;

import ifortex.shuman.uladzislau.authservice.annotation.logging.UserLogging;
import ifortex.shuman.uladzislau.authservice.dto.*;
import ifortex.shuman.uladzislau.authservice.exception.OperationForbiddenException;
import ifortex.shuman.uladzislau.authservice.exception.UserNotFoundException;
import ifortex.shuman.uladzislau.authservice.model.*;
import ifortex.shuman.uladzislau.authservice.service.*;
import ifortex.shuman.uladzislau.authservice.service.validation.UserValidator;
import ifortex.shuman.uladzislau.authservice.util.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

  private final UserService userService;
  private final RoleService roleService;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;
  private final UserTokenService userTokenService;
  private final UserValidator userValidator;
  private final OtpService otpService;
  private final UserMapper userMapper;

  @Override
  @Transactional
  public MessageResponseDto register(RegisterRequestDto request) {
    validateRegistrationRequest(request);
    User newUser = createNewUserFromRequest(request);
    userService.save(newUser);
    log.info("New user registered with email: {}", newUser.getEmail());
    otpService.generateAndSendOtp(newUser.getEmail(), OtpType.EMAIL_VERIFICATION);
    return new MessageResponseDto(
        "Registration successful. Please check your email for a verification code.");
  }

  @Override
  @Transactional
  public MessageResponseDto verifyEmail(VerificationRequestDto request) {
    otpService.validateOtp(request.getEmail(), request.getOtpCode(), OtpType.EMAIL_VERIFICATION);

    User user = userService.findUserByEmailAndStatus(request.getEmail(),
            UserStatus.PENDING_VERIFICATION)
        .orElseThrow(() -> new UserNotFoundException("User not found or already verified."));

    user.setStatus(UserStatus.ACTIVE);
    userService.save(user);

    log.info("Email successfully verified for user {}.", user.getEmail());
    return new MessageResponseDto("Email verified successfully. You can now log in.");
  }

  @Override
  @Transactional
  public LoginResponseDto login(LoginRequestDto request) {
    authenticate(request.getEmail(), request.getPassword());
    User user = userService.getByEmail(request.getEmail());
    if (user.isPasswordTemporary()) {
      return handleTemporaryPasswordLogin(user);
    }
    if (user.is2FAEnabled()) {
      return handle2FALogin(user);
    }
    return handleStandardLogin(user);
  }

  @Override
  public LoginResponseDto verify2FA(VerificationRequestDto request) {
    otpService.validateOtp(request.getEmail(), request.getOtpCode(), OtpType.LOGIN_2FA);
    User user = userService.getByEmail(request.getEmail());
    userValidator.ensureUserIsActive(user);
    log.info("2FA successfully verified for user {}. Issuing Opaque Token.", user.getEmail());

    return generateAndSaveTokens(user);
  }

  @Override
  @UserLogging
  @Transactional // have deleting and saving
  public JwtTokenDto refreshAccessToken(RefreshTokenRequestDto request) {
    UserToken oldRefreshToken = userTokenService.validateAndRetrieveToken(request.getRefreshToken(),
        TokenType.REFRESH);
    User user = oldRefreshToken.getUser();
    userValidator.ensureUserIsActive(user);

    userTokenService.delete(oldRefreshToken);

    String newAccessToken = jwtService.generateToken(user);
    String newRefreshToken = jwtService.generateRefreshToken(user);

    userTokenService.saveUserRefreshToken(user, newRefreshToken);

    log.info("Access and Refresh tokens have been rotated for user {}", user.getEmail());
    return new JwtTokenDto(newAccessToken, newRefreshToken);
  }

  @Override
  public MessageResponseDto requestPasswordReset(String email) {
    log.info("User with email {} requests password reset via OTP", email);
    User user = userService.getByEmail(email);
    otpService.generateAndSendOtp(user.getEmail(), OtpType.PASSWORD_RESET);
    return new MessageResponseDto("A password reset code has been sent to your email.");
  }

  @Override
  @Transactional
  public MessageResponseDto confirmPasswordReset(PasswordResetConfirmDto request) {
    otpService.validateOtp(request.getEmail(), request.getOtpCode(), OtpType.PASSWORD_RESET);
    User user = userService.getByEmail(request.getEmail());
    log.info("User with email {} confirms his password reset via OTP", user.getEmail());
    userValidator.validatePasswordConfirmation(request.getNewPassword(),
        request.getConfirmationPassword());
    userValidator.ensureNewPasswordIsDifferent(user, request.getNewPassword());
    userService.updateUserPasswordAndStatus(user.getId(), request.getNewPassword(),
        UserStatus.ACTIVE);
    log.info("User {} has successfully reset their password.", user.getId());
    return new MessageResponseDto("Your password has been reset successfully.");
  }

  @Override
  @Transactional
  public MessageResponseDto confirmAdminPasswordReset(AdminPasswordResetConfirmDto request) {
    UserToken resetToken = userTokenService.validateAndRetrieveToken(request.getToken(),
        TokenType.PASSWORD_RESET);
    Long userId = resetToken.getUser().getId();
    User user = userService.findById(userId);
    log.info("User with email {} confirms his password", user.getEmail());
    userValidator.validatePasswordConfirmation(request.getNewPassword(),
        request.getConfirmationPassword());
    userValidator.ensureNewPasswordIsDifferent(user, request.getNewPassword());
    userService.updateUserPasswordAndStatus(user.getId(), request.getNewPassword(),
        UserStatus.ACTIVE);
    userTokenService.delete(resetToken);
    log.info("User {} has successfully reset their password via admin link.", user.getId());
    return new MessageResponseDto("Password has been successfully reset.");
  }

  @Override
  public MessageResponseDto resendOtp(ResendOtpRequestDto request) {
    otpService.resendOtp(request.getEmail(), request.getOtpType());
    return new MessageResponseDto("A new verification code has been sent to your email.");
  }

  @Override
  @Transactional // at least consists of 2 operations
  public MessageResponseDto logout(HttpServletRequest request) {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    if (principal instanceof User currentUser) {
      userTokenService.deleteAllByUserAndType(currentUser, TokenType.REFRESH);
      log.info("User {} logged out. All refresh tokens have been deleted.", currentUser.getEmail());
    }

    HttpSession session = request.getSession(false);
    if (session != null) {
      session.invalidate();
      log.info("HttpSession invalidated for logout.");
    }

    SecurityContextHolder.clearContext();
    return new MessageResponseDto("You have been successfully logged out.");
  }

  private void authenticate(String email, String password) {
    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
  }

  private LoginResponseDto handleTemporaryPasswordLogin(User user) {
    log.info("User {} logged in with a temporary password. Forcing change.", user.getId());
    return generateAndSaveTokensWithPasswordChange(user, true);
  }

  private LoginResponseDto handle2FALogin(User user) {
    log.info("2FA is enabled for user {}. Sending OTP.", user.getEmail());
    otpService.generateAndSendOtp(user.getEmail(), OtpType.LOGIN_2FA);
    return LoginResponseDto.builder().twoFARequired(true).build();
  }

  private LoginResponseDto handleStandardLogin(User user) {
    log.info("User {} logged in successfully. Issuing Opaque Token.", user.getEmail());
    return generateAndSaveTokens(user);
  }

  private LoginResponseDto generateAndSaveTokens(User user) {
    return generateAndSaveTokensWithPasswordChange(user, false);
  }

  private LoginResponseDto generateAndSaveTokensWithPasswordChange(User user,
      boolean isPasswordNeedToBeChanged) {
    String accessToken = jwtService.generateToken(user);
    String refreshToken = jwtService.generateRefreshToken(user);
    userTokenService.saveUserRefreshToken(user, refreshToken);
    return LoginResponseDto.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .user(userMapper.toUserDto(user))
        .passwordChangeRequired(isPasswordNeedToBeChanged)
        .build();
  }

  private void validateRegistrationRequest(RegisterRequestDto request) {
    userValidator.validateEmailIsAvailable(request.getEmail());
    userValidator.validatePasswordConfirmation(request.getPassword(),
        request.getPasswordConfirmation());
  }

  private User createNewUserFromRequest(RegisterRequestDto request) {
    UserRole roleName = request.getRole();
    if (roleName != UserRole.ROLE_CLIENT && roleName != UserRole.ROLE_PARAMEDIC) {
      throw new OperationForbiddenException(
          "Registration is only allowed for CLIENT or PARAMEDIC roles.");
    }
    Role role = roleService.findByName(roleName);
    return User.builder()
        .fullName(request.getFullName())
        .email(request.getEmail())
        .phone(request.getPhone())
        .password(passwordEncoder.encode(request.getPassword()))
        .status(UserStatus.PENDING_VERIFICATION)
        .role(role)
        .is2FAEnabled(true)
        .build();
  }
}