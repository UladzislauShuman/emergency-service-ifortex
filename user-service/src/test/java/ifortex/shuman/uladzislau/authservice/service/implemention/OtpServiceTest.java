package ifortex.shuman.uladzislau.authservice.service.implemention;

import ifortex.shuman.uladzislau.authservice.config.properties.OtpProperties;
import ifortex.shuman.uladzislau.authservice.model.OtpType;
import ifortex.shuman.uladzislau.authservice.service.NotificationService;
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

import java.time.Duration;
import java.util.Map;

import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_EMAIL;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_NEW_EMAIL;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_OTP_CODE;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_WRONG_OTP_CODE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private NotificationService notificationService;
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private OtpProperties otpProperties;

    @InjectMocks
    private OtpServiceImpl otpService;

    @Captor
    private ArgumentCaptor<String> stringArgumentCaptor;
    @Captor
    private ArgumentCaptor<Duration> durationArgumentCaptor;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void generateAndSendOtp_ShouldSaveToRedisAndSendEmail() {
        OtpType otpType = OtpType.LOGIN_2FA;
        String expectedRedisKey = otpType.getRedisKeyPrefix() + TEST_EMAIL;
        Duration expectedExpiration = Duration.ofMinutes(15);
        int expectedOtpLength = 6;

        when(otpProperties.getLength()).thenReturn(expectedOtpLength);
        when(otpProperties.getExpiration()).thenReturn(Map.of(otpType.getTemplateKey(), expectedExpiration));

        otpService.generateAndSendOtp(TEST_EMAIL, otpType);

        verify(valueOperations).set(eq(expectedRedisKey), stringArgumentCaptor.capture(), eq(expectedExpiration));
        String generatedOtp = stringArgumentCaptor.getValue();
        assertThat(generatedOtp).hasSize(expectedOtpLength).containsOnlyDigits();
        verify(notificationService).sendOtp(eq(TEST_EMAIL), eq(generatedOtp), eq(otpType));
    }

    @Test
    void validateOtp_WhenCodeIsValid_ShouldDeleteFromRedis() {
        OtpType otpType = OtpType.PASSWORD_RESET;
        String redisKey = otpType.getRedisKeyPrefix() + TEST_EMAIL;
        when(valueOperations.get(redisKey)).thenReturn(TEST_OTP_CODE);

        assertDoesNotThrow(() -> otpService.validateOtp(TEST_EMAIL, TEST_OTP_CODE, otpType));

        verify(redisTemplate).delete(redisKey);
    }

    @Test
    void validateOtp_WhenCodeIsInvalid_ShouldThrowException() {
        OtpType otpType = OtpType.PASSWORD_RESET;
        String redisKey = otpType.getRedisKeyPrefix() + TEST_EMAIL;
        when(valueOperations.get(redisKey)).thenReturn(TEST_OTP_CODE);

        assertThatThrownBy(() -> otpService.validateOtp(TEST_EMAIL, TEST_WRONG_OTP_CODE, otpType))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid or expired OTP code.");

        verify(redisTemplate, never()).delete(redisKey);
    }

    @Test
    void generateAndSendOtpForEmailChange_ShouldSaveCorrectValueToRedis() {
        Long userId = 1L;
        String expectedRedisKey = OtpType.EMAIL_CHANGE.getRedisKeyPrefix() + userId;

        otpService.generateAndSendOtpForEmailChange(userId, TEST_NEW_EMAIL);

        verify(valueOperations).set(eq(expectedRedisKey), stringArgumentCaptor.capture(), any(Duration.class));
        String redisValue = stringArgumentCaptor.getValue();

        assertThat(redisValue).contains(":" + TEST_NEW_EMAIL);
        String generatedOtp = redisValue.split(":")[0];
        assertThat(generatedOtp).hasSize(6).containsOnlyDigits();

        verify(notificationService).sendOtp(eq(TEST_NEW_EMAIL), eq(generatedOtp), eq(OtpType.EMAIL_CHANGE));
    }

    @Test
    void validateAndRetrieveNewEmailFromOtp_WhenCodeIsValid_ShouldReturnEmailAndDeleteKey() {
        Long userId = 1L;
        String redisKey = OtpType.EMAIL_CHANGE.getRedisKeyPrefix() + userId;
        String redisValue = TEST_OTP_CODE + ":" + TEST_NEW_EMAIL;
        when(valueOperations.get(redisKey)).thenReturn(redisValue);

        String resultEmail = otpService.validateAndRetrieveNewEmailFromOtp(userId, TEST_OTP_CODE);

        assertThat(resultEmail).isEqualTo(TEST_NEW_EMAIL);
        verify(redisTemplate).delete(redisKey);
    }

    @Test
    void validateAndRetrieveNewEmailFromOtp_WhenCodeIsNotFound_ShouldThrowException() {
        Long userId = 1L;
        String redisKey = OtpType.EMAIL_CHANGE.getRedisKeyPrefix() + userId;
        when(valueOperations.get(redisKey)).thenReturn(null);

        assertThatThrownBy(() -> otpService.validateAndRetrieveNewEmailFromOtp(userId, TEST_OTP_CODE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid or expired OTP code.");

        verify(redisTemplate, never()).delete(anyString());
    }
}