package ifortex.shuman.uladzislau.authservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmailChangeResponseDto {
    private String message;
    private boolean reLoginRequired;
}