package ifortex.shuman.uladzislau.authservice.paramedic.dto;

import ifortex.shuman.uladzislau.authservice.paramedic.model.KycDecision;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateKycStatusRequestDto {

  @NotNull(message = "New status cannot be null.")
  private KycDecision status;

  private String reason;

  @Valid
  private KycApprovalRequestDto approvalData;
}