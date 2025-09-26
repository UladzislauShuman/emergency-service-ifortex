package ifortex.shuman.uladzislau.authservice.dto;

import ifortex.shuman.uladzislau.authservice.annotation.validation.Password;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminPasswordResetConfirmDto {
    @NotBlank
    private String token;

    @Password
    private String newPassword;

    @NotBlank
    private String confirmationPassword;
}