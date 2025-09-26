package ifortex.shuman.uladzislau.authservice.dto;

import ifortex.shuman.uladzislau.authservice.annotation.validation.PhoneNumber;
import jakarta.validation.constraints.Size;
import lombok.Data;

import static ifortex.shuman.uladzislau.authservice.annotation.validation.messages.
        ValidationMessages.FULL_NAME_MAX_SIZE;

@Data
public class UpdateProfileRequestDto {
    @Size(max = 100, message = FULL_NAME_MAX_SIZE)
    private String fullName;
    
    @PhoneNumber
    private String phone;
}