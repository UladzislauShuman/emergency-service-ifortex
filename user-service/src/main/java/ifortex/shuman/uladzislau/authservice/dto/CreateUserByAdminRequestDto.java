package ifortex.shuman.uladzislau.authservice.dto;

import ifortex.shuman.uladzislau.authservice.annotation.validation.PhoneNumber;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import static ifortex.shuman.uladzislau.authservice.annotation.validation.messages.ValidationMessages.*;

@Data
public class CreateUserByAdminRequestDto {
    @NotBlank(message = EMAIL_NOT_BLANK)
    @Email(message = EMAIL_INVALID_FORMAT)
    @Size(max = 100, message = EMAIL_MAX_SIZE)
    private String email;

    @Size(max = 100, message = FULL_NAME_MAX_SIZE)
    private String fullName;

    @PhoneNumber
    private String phone;
}