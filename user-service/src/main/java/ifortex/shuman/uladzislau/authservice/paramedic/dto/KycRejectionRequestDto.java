package ifortex.shuman.uladzislau.authservice.paramedic.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class KycRejectionRequestDto {

  @NotBlank(message = "Rejection reason cannot be blank")
  private String reason;
}