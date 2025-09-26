package ifortex.shuman.uladzislau.authservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import java.time.Instant;
import lombok.Data;

@Data
public class BlockUserRequestDto {
    @Future(message = "The block expiration date must be in the future.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant blockedUntil;

    private boolean permanent;
}