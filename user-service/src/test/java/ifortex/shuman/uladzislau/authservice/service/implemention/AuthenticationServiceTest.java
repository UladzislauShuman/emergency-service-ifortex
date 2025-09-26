package ifortex.shuman.uladzislau.authservice.service.implemention;

import ifortex.shuman.uladzislau.authservice.dto.AdminPasswordResetConfirmDto;
import ifortex.shuman.uladzislau.authservice.dto.JwtTokenDto;
import ifortex.shuman.uladzislau.authservice.dto.LoginRequestDto;
import ifortex.shuman.uladzislau.authservice.dto.LoginResponseDto;
import ifortex.shuman.uladzislau.authservice.dto.PasswordResetConfirmDto;
import ifortex.shuman.uladzislau.authservice.dto.RefreshTokenRequestDto;
import ifortex.shuman.uladzislau.authservice.dto.RegisterRequestDto;
import ifortex.shuman.uladzislau.authservice.dto.VerificationRequestDto;
import ifortex.shuman.uladzislau.authservice.model.OtpType;
import ifortex.shuman.uladzislau.authservice.model.Role;
import ifortex.shuman.uladzislau.authservice.model.TokenType;
import ifortex.shuman.uladzislau.authservice.model.User;
import ifortex.shuman.uladzislau.authservice.model.UserRole;
import ifortex.shuman.uladzislau.authservice.model.UserStatus;
import ifortex.shuman.uladzislau.authservice.model.UserToken;
import ifortex.shuman.uladzislau.authservice.repository.UserRepository;
import ifortex.shuman.uladzislau.authservice.service.NotificationService;
import ifortex.shuman.uladzislau.authservice.service.JwtService;
import ifortex.shuman.uladzislau.authservice.service.OtpService;
import ifortex.shuman.uladzislau.authservice.service.RoleService;
import ifortex.shuman.uladzislau.authservice.service.UserService;
import ifortex.shuman.uladzislau.authservice.service.UserTokenService;
import ifortex.shuman.uladzislau.authservice.service.validation.UserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static ifortex.shuman.uladzislau.authservice.TestConstanst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private RoleService roleService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private NotificationService notificationService;
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private UserTokenService userTokenService;
    @Mock
    private UserValidator userValidator;
    @Mock
    private OtpService otpService;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @Captor
    private ArgumentCaptor<User> userArgumentCaptor;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .fullName(TEST_FULL_NAME)
                .email(TEST_EMAIL)
                .password(TEST_HASHED_PASSWORD)
                .is2FAEnabled(false)
                .isPasswordTemporary(false)
                .role(new Role())
                .build();
    }

    @Test
    void register_WithValidRequest_ShouldCreateUserAndSendOtp() {
        RegisterRequestDto request = new RegisterRequestDto();
        request.setEmail(TEST_NEW_EMAIL);
        request.setPassword(TEST_VALID_PASSWORD);
        request.setPasswordConfirmation(TEST_VALID_PASSWORD);

        Role clientRole = new Role();
        clientRole.setName(UserRole.ROLE_CLIENT);

        doNothing().when(userValidator).validateEmailIsAvailable(anyString());
        doNothing().when(userValidator).validatePasswordConfirmation(anyString(), anyString());
        when(roleService.getDefaultClientRole()).thenReturn(clientRole);
        when(passwordEncoder.encode(request.getPassword())).thenReturn(TEST_HASHED_PASSWORD);
        when(userService.save(any(User.class))).thenReturn(new User());

        authenticationService.register(request);

        verify(userService, times(1)).save(userArgumentCaptor.capture());
        User savedUser = userArgumentCaptor.getValue();
        assertThat(savedUser.getStatus()).isEqualTo(UserStatus.PENDING_VERIFICATION);
        verify(otpService, times(1)).generateAndSendOtp(eq(request.getEmail()), eq(OtpType.EMAIL_VERIFICATION));
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void register_WhenEmailExists_ShouldThrowIllegalStateException() {
        RegisterRequestDto request = new RegisterRequestDto();
        request.setEmail(TEST_NEW_EMAIL);
        request.setPassword(TEST_VALID_PASSWORD);
        request.setPasswordConfirmation(TEST_VALID_PASSWORD);

        doThrow(new IllegalStateException("User with email ... already exists."))
                .when(userValidator).validateEmailIsAvailable(request.getEmail());

        assertThatThrownBy(() -> authenticationService.register(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already exists");

        verify(userService, never()).save(any());
    }

    @Test
    void register_WhenPasswordsDoNotMatch_ShouldThrowIllegalArgumentException() {
        RegisterRequestDto request = new RegisterRequestDto();
        request.setEmail(TEST_NEW_EMAIL);
        request.setPassword(TEST_VALID_PASSWORD);
        request.setPasswordConfirmation(TEST_WRONG_PASSWORD);

        doNothing().when(userValidator).validateEmailIsAvailable(anyString());
        doThrow(new IllegalArgumentException("Passwords do not match."))
                .when(userValidator).validatePasswordConfirmation(request.getPassword(), request.getPasswordConfirmation());

        assertThatThrownBy(() -> authenticationService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Passwords do not match");
    }

    @Test
    void login_WithStandardUser_ShouldReturnTokens() {
        LoginRequestDto request = new LoginRequestDto();
        request.setEmail(TEST_EMAIL);
        request.setPassword(TEST_VALID_PASSWORD);

        testUser.set2FAEnabled(false);
        testUser.setPasswordTemporary(false);

        when(userService.getByEmail(request.getEmail())).thenReturn(testUser);

        LoginResponseDto response = authenticationService.login(request);

        verify(authenticationManager, times(1)).authenticate(any());
        assertThat(response.isTwoFARequired()).isFalse();
        assertThat(response.isPasswordChangeRequired()).isFalse();
    }

    @Test
    void login_With2FAEnabledUser_ShouldCallOtpService() {
        LoginRequestDto request = new LoginRequestDto();
        request.setEmail(TEST_EMAIL);
        request.setPassword(TEST_VALID_PASSWORD);
        testUser.set2FAEnabled(true);
        when(userService.getByEmail(request.getEmail())).thenReturn(testUser);

        LoginResponseDto response = authenticationService.login(request);

        verify(authenticationManager, times(1)).authenticate(any());
        assertThat(response.isTwoFARequired()).isTrue();
        //assertThat(response.getAccessToken()).isNull();
        verify(otpService).generateAndSendOtp(eq(testUser.getEmail()), eq(OtpType.LOGIN_2FA));
    }

    @Test
    void login_WithTemporaryPassword_ShouldReturnPasswordChangeRequired() {
        LoginRequestDto request = new LoginRequestDto();
        request.setEmail(TEST_EMAIL);
        request.setPassword(TEST_TEMP_PASSWORD);

        testUser.setPasswordTemporary(true);

        when(userService.getByEmail(request.getEmail())).thenReturn(testUser);

        LoginResponseDto response = authenticationService.login(request);

        verify(authenticationManager, times(1)).authenticate(any());

        assertThat(response.isPasswordChangeRequired()).isTrue();
    }

    @Test
    void verify2FA_WithValidOtp_ShouldReturnTokens() {
        VerificationRequestDto request = new VerificationRequestDto();
        request.setEmail(TEST_EMAIL);
        request.setOtpCode(TEST_OTP_CODE);

        doNothing().when(otpService).validateOtp(request.getEmail(), request.getOtpCode(), OtpType.LOGIN_2FA);
        when(userService.getByEmail(request.getEmail())).thenReturn(testUser);

        LoginResponseDto result = authenticationService.verify2FA(request);

        assertThat(result).isNotNull();
        verify(otpService, times(1)).validateOtp(request.getEmail(), request.getOtpCode(), OtpType.LOGIN_2FA);
    }

    @Test
    void verify2FA_WithInvalidOtp_ShouldThrowException() {
        VerificationRequestDto request = new VerificationRequestDto();
        request.setEmail(TEST_EMAIL);
        request.setOtpCode(TEST_WRONG_OTP_CODE);

        doThrow(new IllegalArgumentException("Invalid or expired OTP code."))
                .when(otpService).validateOtp(request.getEmail(), request.getOtpCode(), OtpType.LOGIN_2FA);

        assertThatThrownBy(() -> authenticationService.verify2FA(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid or expired OTP code");

        verify(userService, never()).getByEmail(anyString());
    }

    @Test
    void refreshAccessToken_WithValidToken_ShouldReturnNewAccessToken() {
        String refreshToken = TEST_REFRESH_TOKEN;
        RefreshTokenRequestDto request = new RefreshTokenRequestDto();
        request.setRefreshToken(refreshToken);

        UserToken userToken = UserToken.builder().user(testUser).build();

        when(userTokenService.validateAndRetrieveToken(refreshToken, TokenType.REFRESH)).thenReturn(userToken);
        when(jwtService.isTokenValid(refreshToken, testUser)).thenReturn(true);
        when(jwtService.extractClaim(eq(refreshToken), any())).thenReturn(0);
        when(jwtService.generateToken(testUser)).thenReturn(TEST_NEW_ACCESS_TOKEN);
        when(userService.findById(testUser.getId())).thenReturn(testUser);

        JwtTokenDto result = authenticationService.refreshAccessToken(request);

        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo(TEST_NEW_ACCESS_TOKEN);
        assertThat(result.getRefreshToken()).isEqualTo(refreshToken);
        verify(userTokenService, times(1)).validateAndRetrieveToken(refreshToken, TokenType.REFRESH);
        verify(jwtService, times(1)).isTokenValid(refreshToken, testUser);
        verify(jwtService, times(1)).extractClaim(eq(refreshToken), any());
        verify(jwtService, times(1)).generateToken(testUser);
    }

    @Test
    void refreshAccessToken_WithInvalidJwtToken_ShouldThrowException() {
        String refreshToken = TEST_RESET_TOKEN;
        RefreshTokenRequestDto request = new RefreshTokenRequestDto();
        request.setRefreshToken(refreshToken);

        UserToken userToken = UserToken.builder().user(testUser).build();

        when(userTokenService.validateAndRetrieveToken(refreshToken, TokenType.REFRESH)).thenReturn(userToken);
        when(jwtService.isTokenValid(refreshToken, testUser)).thenReturn(false);

        assertThatThrownBy(() -> authenticationService.refreshAccessToken(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid JWT refresh token");
    }

    @Test
    void requestPasswordReset_ShouldCallOtpService() {
        String email = TEST_EMAIL;
        when(userService.getByEmail(email)).thenReturn(testUser);

        authenticationService.requestPasswordReset(email);

        verify(otpService).generateAndSendOtp(eq(email), eq(OtpType.PASSWORD_RESET));
        verify(userTokenService, never()).createPasswordResetToken(any());
    }


    @Test
    void confirmAdminPasswordReset_WithValidRequest_ShouldUpdatePassword() {
        AdminPasswordResetConfirmDto request = new AdminPasswordResetConfirmDto();
        request.setToken(TEST_RESET_TOKEN);
        request.setNewPassword(TEST_VALID_PASSWORD);
        request.setConfirmationPassword(TEST_VALID_PASSWORD);

        UserToken resetToken = UserToken.builder().user(testUser).token(request.getToken()).build();

        when(userService.findById(testUser.getId())).thenReturn(testUser);
        when(userTokenService.validateAndRetrieveToken(request.getToken(), TokenType.PASSWORD_RESET)).thenReturn(resetToken);

        authenticationService.confirmAdminPasswordReset(request);

        verify(userService, times(1)).updateUserPasswordAndStatus(testUser.getId(), request.getNewPassword(), UserStatus.ACTIVE);
        verify(userTokenService, times(1)).delete(resetToken);
    }

    @Test
    void confirmAdminPasswordReset_WhenPasswordsDoNotMatch_ShouldThrowException() {
        AdminPasswordResetConfirmDto request = new AdminPasswordResetConfirmDto();
        request.setToken(TEST_RESET_TOKEN);
        request.setNewPassword(TEST_VALID_PASSWORD);
        request.setConfirmationPassword(TEST_WRONG_PASSWORD);

        UserToken mockedResetToken = mock(UserToken.class);
        when(mockedResetToken.getUser()).thenReturn(testUser);
        when(userTokenService.validateAndRetrieveToken(request.getToken(), TokenType.PASSWORD_RESET))
                .thenReturn(mockedResetToken);
        when(userService.findById(testUser.getId())).thenReturn(testUser);

        doThrow(new IllegalArgumentException("Passwords do not match."))
                .when(userValidator)
                .validatePasswordConfirmation(request.getNewPassword(), request.getConfirmationPassword());

        assertThatThrownBy(() -> authenticationService.confirmAdminPasswordReset(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Passwords do not match.");

        verify(userService, never()).updateUserPasswordAndStatus(anyLong(), anyString(), any(UserStatus.class));
        verify(userTokenService, never()).delete(any(UserToken.class));
    }

    @Test
    void confirmPasswordReset_WithValidOtp_ShouldUpdatePassword() {
        PasswordResetConfirmDto request = new PasswordResetConfirmDto();
        request.setEmail(TEST_EMAIL);
        request.setOtpCode(TEST_OTP_CODE);
        request.setNewPassword(TEST_VALID_PASSWORD);
        request.setConfirmationPassword(TEST_VALID_PASSWORD);

        doNothing().when(otpService).validateOtp(
                request.getEmail(),
                request.getOtpCode(),
                OtpType.PASSWORD_RESET
        );
        when(userService.getByEmail(TEST_EMAIL)).thenReturn(testUser);
        doNothing().when(userValidator).validatePasswordConfirmation(
                request.getNewPassword(),
                request.getConfirmationPassword()
        );

        authenticationService.confirmPasswordReset(request);

        verify(userService, times(1)).updateUserPasswordAndStatus(
                eq(testUser.getId()),
                eq(request.getNewPassword()),
                eq(UserStatus.ACTIVE)
        );
        verify(otpService, times(1)).validateOtp(anyString(), anyString(), eq(OtpType.PASSWORD_RESET));
        verify(userValidator, times(1)).validatePasswordConfirmation(anyString(), anyString());
    }

    @Test
    void confirmPasswordReset_WhenPasswordsDoNotMatch_ShouldThrowException() {

        PasswordResetConfirmDto request = new PasswordResetConfirmDto();
        request.setEmail(TEST_EMAIL);
        request.setOtpCode(TEST_OTP_CODE);
        request.setNewPassword(TEST_VALID_PASSWORD);
        request.setConfirmationPassword(TEST_WRONG_PASSWORD);

        doNothing().when(otpService).validateOtp(
                request.getEmail(),
                request.getOtpCode(),
                OtpType.PASSWORD_RESET
        );
        when(userService.getByEmail(TEST_EMAIL)).thenReturn(testUser);
        doThrow(new IllegalArgumentException("Passwords do not match."))
                .when(userValidator)
                .validatePasswordConfirmation(request.getNewPassword(), request.getConfirmationPassword());

        assertThatThrownBy(() -> authenticationService.confirmPasswordReset(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Passwords do not match.");

        verify(userService, never()).updateUserPasswordAndStatus(anyLong(), anyString(), any(UserStatus.class));
        verify(otpService, times(1)).validateOtp(
                request.getEmail(),
                request.getOtpCode(),
                OtpType.PASSWORD_RESET
        );
    }
}