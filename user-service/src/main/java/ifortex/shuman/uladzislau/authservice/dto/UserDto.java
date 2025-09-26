package ifortex.shuman.uladzislau.authservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import ifortex.shuman.uladzislau.authservice.model.UserRole;
import ifortex.shuman.uladzislau.authservice.model.UserStatus;
import java.time.Instant;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {

  private Long id;
  private String email;
  private String fullName;
  private String phone;
  private UserStatus status;
  private boolean is2FAEnabled;
  private boolean hasGoogleAccount;
  private UserRole role;
  private boolean isPermanentlyBlocked;

  @JsonFormat(shape = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
  private Instant blockedUntil;
}