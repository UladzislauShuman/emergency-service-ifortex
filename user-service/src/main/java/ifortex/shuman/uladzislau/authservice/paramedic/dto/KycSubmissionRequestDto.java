package ifortex.shuman.uladzislau.authservice.paramedic.dto;

import ifortex.shuman.uladzislau.authservice.annotation.validation.PhoneNumber;
import ifortex.shuman.uladzislau.authservice.annotation.validation.ValidFile;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class KycSubmissionRequestDto {

  @NotBlank(message = "Full name cannot be blank")
  @Size(max = 100)
  private String fullName;

  @NotBlank(message = "Email cannot be blank")
  @Email
  @Size(max = 100)
  private String email;

  @PhoneNumber
  private String phone;

  @NotNull(message = "Identity document is required.")
  @ValidFile(allowedTypes = {"image/jpeg", "image/png", "application/pdf"})
  private MultipartFile identityDocument;

  @NotNull(message = "Selfie with document is required.")
  @ValidFile(allowedTypes = {"image/jpeg", "image/png"})
  private MultipartFile selfieWithDocument;

  @NotNull(message = "Medical certificate is required.")
  @ValidFile(allowedTypes = {"image/jpeg", "image/png", "application/pdf"})
  private MultipartFile medicalCertificate;
}