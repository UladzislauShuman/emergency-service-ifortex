package ifortex.shuman.uladzislau.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import static ifortex.shuman.uladzislau.authservice.annotation.validation.messages.
        ValidationMessages.EMAIL_NOT_BLANK;
import static ifortex.shuman.uladzislau.authservice.annotation.validation.messages.
        ValidationMessages.OTP_NOT_BLANK;

@Data
public class VerificationRequestDto {

    @NotBlank(message = EMAIL_NOT_BLANK)
    @Email
    private String email;

    @NotBlank(message = OTP_NOT_BLANK)
    private String otpCode;
}