package ifortex.shuman.uladzislau.authservice.paramedic.dto;

import ifortex.shuman.uladzislau.authservice.paramedic.model.ParamedicApplicationStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KycStatusResponseDto {

  private ParamedicApplicationStatus applicationStatus;
  private String rejectionReason; // only if status is rejected
}