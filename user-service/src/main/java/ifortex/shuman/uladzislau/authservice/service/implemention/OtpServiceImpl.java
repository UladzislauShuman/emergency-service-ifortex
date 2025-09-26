package ifortex.shuman.uladzislau.authservice.service.implemention;

import ifortex.shuman.uladzislau.authservice.config.properties.OtpProperties;
import ifortex.shuman.uladzislau.authservice.exception.InvalidOtpException;
import ifortex.shuman.uladzislau.authservice.exception.ResourceConflictException;
import ifortex.shuman.uladzislau.authservice.model.OtpType;
import ifortex.shuman.uladzislau.authservice.model.StoredOtpData;
import ifortex.shuman.uladzislau.authservice.service.NotificationService;
import ifortex.shuman.uladzislau.authservice.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpServiceImpl implements OtpService {

    public static final String DELAY = "${application.retry.redis.initial-delay}";
    public static final String MAX_ATTEMPTS = "${application.retry.redis.max-attempts}";
    public static final String MULTIPLIER = "${application.retry.redis.multiplier}";
    private static final String RESEND_COOLDOWN_PREFIX = "resend_cooldown:";

    private final NotificationService notificationService;
    private final RedisTemplate<String, String> redisTemplate;
    private final OtpProperties otpProperties;

    @Override
    @Retryable(
            value = {RedisConnectionFailureException.class},
            maxAttemptsExpression = MAX_ATTEMPTS,
            backoff = @Backoff(
                    delayExpression = DELAY,
                    multiplierExpression = MULTIPLIER
            )
    )
    public void generateAndSendOtp(String email, OtpType otpType) {
        String otp = generateOtp();
        Duration expiration = getExpirationForType(otpType);
        String redisKey = otpType.getRedisKeyPrefix() + email;

        log.info("Generating and storing OTP for email: {}, type: {}, expiration: {}", email, otpType, expiration);
        redisTemplate.opsForValue().set(redisKey, otp, expiration);

        notificationService.sendOtp(email, otp, otpType);
    }

    @Override
    @Retryable(
            value = {RedisConnectionFailureException.class},
            maxAttemptsExpression = MAX_ATTEMPTS,
            backoff = @Backoff(delayExpression = DELAY)
    )
    public void validateOtp(String email, String otpCode, OtpType otpType) {
        String redisKey = otpType.getRedisKeyPrefix() + email;
        String storedOtp = redisTemplate.opsForValue().get(redisKey);

        if (storedOtp == null || !storedOtp.equals(otpCode)) {
            throw new InvalidOtpException("Invalid or expired OTP code.");
        }

        redisTemplate.delete(redisKey);
    }

    @Override
    @Retryable(
            value = {RedisConnectionFailureException.class},
            maxAttemptsExpression = MAX_ATTEMPTS,
            backoff = @Backoff(
                    delayExpression = DELAY,
                    multiplierExpression = MULTIPLIER
            )
    )
    public void generateAndSendOtpForEmailChange(Long userId, String newEmail) {
        String otp = generateOtp();
        Duration expiration = getExpirationForType(OtpType.EMAIL_CHANGE);
        String redisKey = OtpType.EMAIL_CHANGE.getRedisKeyPrefix() + userId;
        String redisValue = otp + ":" + newEmail;

        log.info("Generating and storing OTP for email change for userId: {}, expiration: {}", userId, expiration);
        redisTemplate.opsForValue().set(redisKey, redisValue, expiration);

        notificationService.sendOtp(newEmail, otp, OtpType.EMAIL_CHANGE);
    }

    @Override
    @Retryable(
            value = {RedisConnectionFailureException.class},
            maxAttemptsExpression = MAX_ATTEMPTS,
            backoff = @Backoff(
                    delayExpression = DELAY,
                    multiplierExpression = MULTIPLIER
            )
    )
    public String validateAndRetrieveNewEmailFromOtp(Long userId, String otpCode) {
        String redisKey = OtpType.EMAIL_CHANGE.getRedisKeyPrefix() + userId;
        StoredOtpData storedData = getAndParseStoredOtpData(redisKey);

        if (!storedData.otp().equals(otpCode)) {
            log.warn("Invalid OTP attempt for email change for userId: {}", userId);
            throw new IllegalArgumentException("Invalid OTP code.");
        }

        redisTemplate.delete(redisKey);
        return storedData.email();
    }

    @Override
    public void resendOtp(String email, OtpType otpType) {
        String cooldownKey = RESEND_COOLDOWN_PREFIX + otpType.name() + ":" + email;

        if (Boolean.TRUE.equals(redisTemplate.hasKey(cooldownKey))) {
            log.warn("OTP resend request for {} is rejected due to active cooldown.", email);
            throw new ResourceConflictException("Please wait before requesting another code.");
        }
        log.info("Resending OTP for {} of type {}", email, otpType);
        generateAndSendOtp(email, otpType);
        Duration cooldownDuration = Duration.ofSeconds(otpProperties.getResendCooldownSeconds());
        redisTemplate.opsForValue().set(cooldownKey, "locked", cooldownDuration);
        log.info("Set resend cooldown for {} for {} seconds.", email, cooldownDuration.toSeconds());
    }

    @Recover
    public void recoverGenerateAndSendOtp(RedisConnectionFailureException e, String email, OtpType otpType) {
        log.error("Failed to generate and send OTP for email {} and type {} after multiple Redis attempts. Error: {}",
                email, otpType, e.getMessage());
        throw new IllegalStateException(
                "System is currently unavailable to send verification codes. Please try again later.", e);
    }

    @Recover
    public void recoverValidateOtp(RedisConnectionFailureException e, String email, String otpCode, OtpType otpType) {
        log.error("Failed to validate OTP for email {} after multiple Redis attempts. Error: {}",
                email, e.getMessage());
        throw new IllegalStateException(
                "Unable to contact verification service. Please try again later.", e);
    }

    @Recover
    public void recoverGenerateAndSendOtpForEmailChange(
            RedisConnectionFailureException e, Long userId, String newEmail) {
        log.error("Failed to generate OTP for email change for userId {} after multiple Redis attempts. Error: {}",
                userId, e.getMessage());
        throw new IllegalStateException(
                "System is currently unavailable to process your request. Please try again later.", e);
    }

    @Recover
    public String recoverValidateAndRetrieveNewEmailFromOtp(
            RedisConnectionFailureException e, Long userId, String otpCode) {
        log.error("Failed to validate OTP for email change for userId {} after multiple Redis attempts. Error: {}",
                userId, e.getMessage());
        throw new IllegalStateException(
                "System is currently unavailable to verify your request. Please try again later.", e);
    }

    @Recover
    public void recoverFromBusinessException(InvalidOtpException e, String email, String otpCode, OtpType otpType) {
        throw e;
    }

    @Recover
    public String recoverFromBusinessException(InvalidOtpException e, Long userId, String otpCode) {
        throw e;
    }

    @Recover
    public String recoverFromBusinessException(IllegalArgumentException e, Long userId, String otpCode) {
        throw e;
    }

    private StoredOtpData getAndParseStoredOtpData(String redisKey) {
        String storedValue = redisTemplate.opsForValue().get(redisKey);
        if (storedValue == null) {
            throw new InvalidOtpException("Invalid or expired OTP code.");
        }
        String[] parts = storedValue.split(":", 2);
        if (parts.length != 2) {
            log.error("Corrupted OTP data in Redis. Key: '{}', Value: '{}'", redisKey, storedValue);
            throw new InvalidOtpException("Invalid or expired OTP code.");
        }
        return new StoredOtpData(parts[0], parts[1]);
    }

    private String generateOtp() {
        int length = otpProperties.getLength();
        if (length <= 0) {
            length = 6;
            log.warn("OTP length is not configured or invalid, using default value: {}", length);
        }
        int bound = (int) Math.pow(10, length);
        int otpValue = new Random().nextInt(bound);
        return String.format("%0" + length + "d", otpValue);
    }

    private Duration getExpirationForType(OtpType otpType) {
        Duration expiration = otpProperties.getExpiration().get(otpType.getTemplateKey());
        if (expiration == null) {
            log.error("Expiration duration is not configured for OtpType: {}. Using default 5 minutes.", otpType);
            return Duration.ofMinutes(5);
        }
        return expiration;
    }
}