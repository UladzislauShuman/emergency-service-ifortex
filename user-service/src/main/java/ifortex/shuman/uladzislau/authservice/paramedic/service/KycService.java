package ifortex.shuman.uladzislau.authservice.paramedic.service;

import ifortex.shuman.uladzislau.authservice.dto.ActionResponseDto;
import ifortex.shuman.uladzislau.authservice.dto.MessageResponseDto;
import ifortex.shuman.uladzislau.authservice.dto.VerificationRequestDto;
import ifortex.shuman.uladzislau.authservice.paramedic.dto.KycApplicationDetailsDto;
import ifortex.shuman.uladzislau.authservice.paramedic.dto.KycApplicationSummaryDto;
import ifortex.shuman.uladzislau.authservice.paramedic.dto.KycStatusResponseDto;
import ifortex.shuman.uladzislau.authservice.paramedic.dto.KycSubmissionRequestDto;
import ifortex.shuman.uladzislau.authservice.paramedic.dto.UpdateKycStatusRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface KycService {

  MessageResponseDto submitApplication(KycSubmissionRequestDto request);

  MessageResponseDto verifyApplicationEmail(VerificationRequestDto request);

  KycStatusResponseDto getKycStatus(String email);

  Page<KycApplicationDetailsDto> getPendingApplications(Pageable pageable);

  KycApplicationDetailsDto getApplicationDetails(Long applicationId);

  ActionResponseDto updateApplicationStatus(Long applicationId, UpdateKycStatusRequestDto request);
}