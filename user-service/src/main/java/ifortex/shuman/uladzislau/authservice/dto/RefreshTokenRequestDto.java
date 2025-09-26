package ifortex.shuman.uladzislau.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import static ifortex.shuman.uladzislau.authservice.annotation.validation.messages.
        ValidationMessages.REFRESH_TOKEN_NOT_BLANK;

@Data
public class RefreshTokenRequestDto {
    @NotBlank(message = REFRESH_TOKEN_NOT_BLANK)
    private String refreshToken;
}