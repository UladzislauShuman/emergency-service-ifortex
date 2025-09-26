package ifortex.shuman.uladzislau.authservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateUserResponseDto {
    private UserDto user;
    private String temporaryPassword;
}