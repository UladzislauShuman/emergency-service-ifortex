package ifortex.shuman.uladzislau.authservice.dto;

import ifortex.shuman.uladzislau.authservice.model.OtpType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResendOtpRequestDto {

  @NotBlank(message = "Email cannot be blank")
  @Email
  private String email;

  @NotNull(message = "OTP type cannot be null")
  private OtpType otpType;
}