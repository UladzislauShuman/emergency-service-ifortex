package ifortex.shuman.uladzislau.authservice.dto;

import ifortex.shuman.uladzislau.authservice.annotation.validation.messages.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailChangeConfirmDto {
    @NotBlank(message = ValidationMessages.OTP_NOT_BLANK)
    private String otpCode;
}