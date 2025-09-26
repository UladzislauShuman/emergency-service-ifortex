package ifortex.shuman.uladzislau.authservice.paramedic.dto;

import ifortex.shuman.uladzislau.authservice.dto.UserDto;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class KycApplicationDetailsDto {

  private Long applicationId;
  private UserDto user;
  private String identityDocumentPath;
  private String selfieWithDocumentPath;
  private String medicalCertificatePath;
  private Instant submittedAt;
}