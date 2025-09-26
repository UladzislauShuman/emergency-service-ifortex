package ifortex.shuman.uladzislau.authservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminPasswordResetResponseDto {
    private String message;
    private String temporaryPassword;
}