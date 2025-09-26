package ifortex.shuman.uladzislau.authservice.dto;

import ifortex.shuman.uladzislau.authservice.annotation.validation.Password;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import static ifortex.shuman.uladzislau.authservice.annotation.validation.messages.
        ValidationMessages.EMAIL_NOT_BLANK;
import static ifortex.shuman.uladzislau.authservice.annotation.validation.messages.
        ValidationMessages.OTP_NOT_BLANK;
import static ifortex.shuman.uladzislau.authservice.annotation.validation.messages.
        ValidationMessages.PASSWORD_CONFIRMATION_NOT_BLANK;
import static ifortex.shuman.uladzislau.authservice.annotation.validation.messages.
        ValidationMessages.PASSWORD_NOT_BLANK;

@Data
public class PasswordResetConfirmDto {
    @NotBlank(message = EMAIL_NOT_BLANK)
    @Email
    private String email;

    @NotBlank(message = OTP_NOT_BLANK)
    private String otpCode;

    @Password
    @NotBlank(message = PASSWORD_NOT_BLANK)
    private String newPassword;

    @NotBlank(message = PASSWORD_CONFIRMATION_NOT_BLANK)
    private String confirmationPassword;
}