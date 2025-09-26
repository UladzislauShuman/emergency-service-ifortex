package ifortex.shuman.uladzislau.authservice.dto;

import ifortex.shuman.uladzislau.authservice.annotation.validation.Password;
import ifortex.shuman.uladzislau.authservice.annotation.validation.PhoneNumber;
import ifortex.shuman.uladzislau.authservice.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import static ifortex.shuman.uladzislau.authservice.annotation.validation.messages.
        ValidationMessages.EMAIL_INVALID_FORMAT;
import static ifortex.shuman.uladzislau.authservice.annotation.validation.messages.
        ValidationMessages.EMAIL_MAX_SIZE;
import static ifortex.shuman.uladzislau.authservice.annotation.validation.messages.
        ValidationMessages.EMAIL_NOT_BLANK;
import static ifortex.shuman.uladzislau.authservice.annotation.validation.messages.
        ValidationMessages.FULL_NAME_MAX_SIZE;
import static ifortex.shuman.uladzislau.authservice.annotation.validation.messages.
        ValidationMessages.FULL_NAME_NOT_BLANK;
import static ifortex.shuman.uladzislau.authservice.annotation.validation.messages.
        ValidationMessages.PASSWORD_CONFIRMATION_NOT_BLANK;
import static ifortex.shuman.uladzislau.authservice.annotation.validation.messages.
        ValidationMessages.PASSWORD_NOT_BLANK;
import static ifortex.shuman.uladzislau.authservice.annotation.validation.messages.
        ValidationMessages.PHONE_NOT_BLANK;

@Data
public class RegisterRequestDto {

    @NotNull(message = "Role must be selected")
    private UserRole role;

    @NotBlank(message = FULL_NAME_NOT_BLANK)
    @Size(max = 100, message = FULL_NAME_MAX_SIZE)
    private String fullName;

    @NotBlank(message = EMAIL_NOT_BLANK)
    @Email(message =EMAIL_INVALID_FORMAT)
    @Size(max = 100, message = EMAIL_MAX_SIZE)
    private String email;

    @NotBlank(message =PHONE_NOT_BLANK)
    @PhoneNumber
    private String phone;

    @Password
    @NotBlank(message = PASSWORD_NOT_BLANK)
    private String password;

    @NotBlank(message = PASSWORD_CONFIRMATION_NOT_BLANK)
    private String passwordConfirmation;
}