package ifortex.shuman.uladzislau.authservice.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OtpType {
    
    EMAIL_VERIFICATION("email-verification", "email_verification_otp:"),
    LOGIN_2FA("login-2fa", "login_2fa_otp:"),
    PASSWORD_RESET("password-reset", "password_reset_otp:"),
    EMAIL_CHANGE("email-change", "email_change_otp:");

    private final String templateKey;
    private final String redisKeyPrefix;
}