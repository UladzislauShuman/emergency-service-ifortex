package ifortex.shuman.uladzislau.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import static ifortex.shuman.uladzislau.authservice.annotation.validation.messages.
        ValidationMessages.EMAIL_INVALID_FORMAT;
import static ifortex.shuman.uladzislau.authservice.annotation.validation.messages.
        ValidationMessages.EMAIL_NOT_BLANK;
import static ifortex.shuman.uladzislau.authservice.annotation.validation.messages.
        ValidationMessages.PASSWORD_NOT_BLANK;

@Data
public class LoginRequestDto {

    @NotBlank(message = EMAIL_NOT_BLANK)
    @Email(message = EMAIL_INVALID_FORMAT)
    private String email;

    @NotBlank(message = PASSWORD_NOT_BLANK)
    private String password;
}