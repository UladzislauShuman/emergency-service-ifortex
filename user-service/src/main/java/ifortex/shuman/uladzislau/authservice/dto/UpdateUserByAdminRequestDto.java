package ifortex.shuman.uladzislau.authservice.dto;

import ifortex.shuman.uladzislau.authservice.annotation.validation.PhoneNumber;
import ifortex.shuman.uladzislau.authservice.model.UserRole;
import lombok.Data;

@Data
public class UpdateUserByAdminRequestDto {
    private String fullName;
    @PhoneNumber
    private String phone;
}