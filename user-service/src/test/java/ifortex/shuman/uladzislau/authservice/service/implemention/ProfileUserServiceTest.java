package ifortex.shuman.uladzislau.authservice.service.implemention;

import ifortex.shuman.uladzislau.authservice.dto.ChangePasswordRequestDto;
import ifortex.shuman.uladzislau.authservice.dto.EmailChangeConfirmDto;
import ifortex.shuman.uladzislau.authservice.dto.EmailChangeRequestDto;
import ifortex.shuman.uladzislau.authservice.dto.EmailChangeResponseDto;
import ifortex.shuman.uladzislau.authservice.dto.UpdateProfileRequestDto;
import ifortex.shuman.uladzislau.authservice.dto.UserDto;
import ifortex.shuman.uladzislau.authservice.model.TokenType;
import ifortex.shuman.uladzislau.authservice.model.User;
import ifortex.shuman.uladzislau.authservice.model.UserStatus;
import ifortex.shuman.uladzislau.authservice.repository.UserRepository;
import ifortex.shuman.uladzislau.authservice.service.NotificationService;
import ifortex.shuman.uladzislau.authservice.service.OtpService;
import ifortex.shuman.uladzislau.authservice.service.UserService;
import ifortex.shuman.uladzislau.authservice.service.UserTokenService;
import ifortex.shuman.uladzislau.authservice.service.validation.UserValidator;
import ifortex.shuman.uladzislau.authservice.util.mapper.UserMapper;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_EMAIL;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_FULL_NAME;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_GOOGLE_ID;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_HASHED_PASSWORD;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_NEW_EMAIL;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_NEW_VALID_PASSWORD;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_OTP_CODE;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_PHONE;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_UPDATE_USER_FULL_NAME;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_VALID_PASSWORD;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_WRONG_OTP_CODE;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_WRONG_PASSWORD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileUserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserTokenService userTokenService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private UserService userService;
    @Mock
    private UserMapper userMapper;
    @Mock
    private UserValidator userValidator;
    @Mock
    private OtpService otpService;

    @Mock
    private ValueOperations<String, String> valueOperations;
    @Captor
    private ArgumentCaptor<User> userCaptor;

    @InjectMocks
    private ProfileUserServiceImpl profileUserService;

    private User testUser;


    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email(TEST_EMAIL)
                .password(TEST_HASHED_PASSWORD)
                .fullName(TEST_FULL_NAME)
                .status(UserStatus.ACTIVE)
                .build();
        when(userService.findById(testUser.getId())).thenReturn(testUser);
    }

    @Test
    void updateProfile_WithValidData_ShouldUpdateUser() {
        UpdateProfileRequestDto request = new UpdateProfileRequestDto();
        request.setFullName(TEST_UPDATE_USER_FULL_NAME);
        request.setPhone(TEST_PHONE);

        profileUserService.updateProfile(testUser.getId(), request);

        verify(userService).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getFullName()).isEqualTo(TEST_UPDATE_USER_FULL_NAME);
        assertThat(savedUser.getPhone()).isEqualTo(TEST_PHONE);
    }

    @Test
    void changePassword_WithCorrectCurrentPassword_ShouldUpdatePassword() {
        ChangePasswordRequestDto request = new ChangePasswordRequestDto();
        request.setCurrentPassword(TEST_VALID_PASSWORD);
        request.setNewPassword(TEST_NEW_VALID_PASSWORD);
        request.setConfirmationPassword(TEST_NEW_VALID_PASSWORD);

        doNothing().when(userValidator).checkPasswordMatch(any(User.class), anyString());
        doNothing().when(userValidator).validatePasswordConfirmation(anyString(), anyString());

        profileUserService.changePassword(testUser, request);

        verify(userService).updateUserPassword(testUser.getId(), TEST_NEW_VALID_PASSWORD);
    }

    @Test
    void changePassword_WithWrongCurrentPassword_ShouldThrowException() {
        ChangePasswordRequestDto request = new ChangePasswordRequestDto();
        request.setCurrentPassword(TEST_WRONG_PASSWORD);

        doThrow(new IllegalArgumentException("Wrong current password."))
                .when(userValidator).checkPasswordMatch(testUser, request.getCurrentPassword());

        assertThatThrownBy(() -> profileUserService.changePassword(testUser, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Wrong current password.");

        verify(userService, never()).updateUserPassword(anyLong(), anyString());
    }

    @Test
    void setTwoFactorAuthentication_ShouldUpdateUserFlag() {
        profileUserService.setTwoFactorAuthentication(testUser, true);

        verify(userService).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.is2FAEnabled()).isTrue();
    }

    @Test
    void requestEmailChange_WithValidData_ShouldCallOtpService() {
        EmailChangeRequestDto request = new EmailChangeRequestDto();
        request.setCurrentPassword(TEST_VALID_PASSWORD);
        request.setNewEmail(TEST_NEW_EMAIL);

        doNothing().when(userValidator).checkPasswordMatch(any(User.class), anyString());
        doNothing().when(userValidator).validateEmailIsAvailable(anyString());

        profileUserService.requestEmailChange(testUser, request);

        verify(notificationService).sendEmailChangeNotificationToOldEmail(testUser.getEmail(), request.getNewEmail());
        verify(otpService).generateAndSendOtpForEmailChange(testUser.getId(), request.getNewEmail());
    }

    @Test
    void requestEmailChange_WhenNewEmailExists_ShouldThrowException() {
        EmailChangeRequestDto request = new EmailChangeRequestDto();
        request.setCurrentPassword(TEST_VALID_PASSWORD);
        request.setNewEmail(TEST_NEW_EMAIL);

        doNothing().when(userValidator).checkPasswordMatch(any(User.class), anyString());
        doThrow(new IllegalStateException("Email already exists."))
                .when(userValidator).validateEmailIsAvailable(request.getNewEmail());

        assertThatThrownBy(() -> profileUserService.requestEmailChange(testUser, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Email already exists.");

        verify(otpService, never()).generateAndSendOtpForEmailChange(anyLong(), anyString());
    }

    @Test
    void confirmEmailChange_WithValidOtp_ShouldChangeEmailAndInvalidateTokens() {
        EmailChangeConfirmDto request = new EmailChangeConfirmDto();
        request.setOtpCode(TEST_OTP_CODE);

        when(otpService.validateAndRetrieveNewEmailFromOtp(testUser.getId(), request.getOtpCode()))
                .thenReturn(TEST_NEW_EMAIL);
        EmailChangeResponseDto response = profileUserService.confirmEmailChange(testUser, request);

        verify(userService).updateUserEmailAndInvalidateTokens(testUser.getId(), TEST_NEW_EMAIL);
        verify(userTokenService).deleteAllByUserAndType(testUser, TokenType.REFRESH);
        assertThat(response.isReLoginRequired()).isTrue();
    }

    @Test
    void confirmEmailChange_WithInvalidOtp_ShouldThrowException() {
        EmailChangeConfirmDto request = new EmailChangeConfirmDto();
        request.setOtpCode(TEST_WRONG_OTP_CODE);

        doThrow(new IllegalArgumentException("Invalid OTP code."))
                .when(otpService).validateAndRetrieveNewEmailFromOtp(testUser.getId(), request.getOtpCode());
        assertThatThrownBy(() -> profileUserService.confirmEmailChange(testUser, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid OTP code.");

        verify(userService, never()).updateUserEmailAndInvalidateTokens(anyLong(), anyString());
    }

    @Test
    void linkGoogleAccount_WithMatchingEmail_ShouldSetGoogleId() {
        Long userId = testUser.getId();
        String googleId = TEST_GOOGLE_ID;
        String googleEmail = testUser.getEmail();

        when(userService.findById(userId)).thenReturn(testUser);

        profileUserService.linkGoogleAccount(userId, googleId, googleEmail);

        verify(userService).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getGoogleId()).isEqualTo(googleId);
    }

    @Test
    void linkGoogleAccount_WithMismatchedEmail_ShouldThrowException() {
        Long userId = testUser.getId();
        String googleId = TEST_GOOGLE_ID;
        String googleEmail = TEST_NEW_EMAIL;

        when(userService.findById(userId)).thenReturn(testUser);

        assertThatThrownBy(() -> profileUserService.linkGoogleAccount(userId, googleId, googleEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Google account email does not match your profile email.");

        verify(userService, never()).save(any());
    }


    @Test
    void getUserProfile_ShouldCallMapperAndReturnDto() {
        UserDto expectedDto = UserDto.builder().build();
        when(userMapper.toUserDto(testUser)).thenReturn(expectedDto);

        UserDto resultDto = profileUserService.getUserProfile(testUser.getId());

        verify(userMapper, times(1)).toUserDto(testUser);
        assertThat(resultDto).isEqualTo(expectedDto);
    }

    @Test
    void updateProfile_WithOnlyFullName_ShouldUpdateOnlyFullName() {
        UpdateProfileRequestDto request = new UpdateProfileRequestDto();
        request.setFullName(TEST_FULL_NAME);
        request.setPhone(null);

        String originalPhone = testUser.getPhone();

        profileUserService.updateProfile(testUser.getId(), request);

        verify(userService).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getFullName()).isEqualTo(TEST_FULL_NAME);
        assertThat(savedUser.getPhone()).isEqualTo(originalPhone);
    }

    @Test
    void confirmEmailChange_WhenOtpIsInvalidOrExpired_ShouldThrowException() {
        EmailChangeConfirmDto request = new EmailChangeConfirmDto();
        request.setOtpCode("any-invalid-otp");

        doThrow(new IllegalArgumentException("Invalid or expired OTP code."))
                .when(otpService).validateAndRetrieveNewEmailFromOtp(testUser.getId(), request.getOtpCode());

        assertThatThrownBy(() -> profileUserService.confirmEmailChange(testUser, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid or expired OTP code.");

        verify(userService, never()).updateUserEmailAndInvalidateTokens(anyLong(), anyString());
        verify(userTokenService, never()).deleteAllByUserAndType(any(User.class), any(TokenType.class));
    }

    @Test
    void setTwoFactorAuthentication_WhenDisabling_ShouldUpdateUserFlagToFalse() {
        testUser.set2FAEnabled(true);

        profileUserService.setTwoFactorAuthentication(testUser, false);

        verify(userService).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.is2FAEnabled()).isFalse();
    }
}