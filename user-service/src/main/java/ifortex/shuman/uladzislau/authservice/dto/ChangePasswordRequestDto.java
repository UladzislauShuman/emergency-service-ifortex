package ifortex.shuman.uladzislau.authservice.dto;

import ifortex.shuman.uladzislau.authservice.annotation.validation.Password;
import static ifortex.shuman.uladzislau.authservice.annotation.validation.messages.
        ValidationMessages.PASSWORD_CONFIRMATION_NOT_BLANK;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangePasswordRequestDto {
    @NotBlank
    private String currentPassword;

    @Password
    private String newPassword;

    @NotBlank(message = PASSWORD_CONFIRMATION_NOT_BLANK)
    private String confirmationPassword;
}