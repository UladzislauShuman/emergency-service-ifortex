package ifortex.shuman.uladzislau.authservice.paramedic.dto;

import ifortex.shuman.uladzislau.authservice.paramedic.model.ParamedicApplicationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class KycApplicationSummaryDto {
    private Long id;
    private ParamedicApplicationStatus status;
    private Instant submittedAt;
    private Instant reviewedAt;
    private String rejectionReason;
}