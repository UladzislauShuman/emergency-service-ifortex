package ifortex.shuman.uladzislau.authservice.paramedic.controller;

import ifortex.shuman.uladzislau.authservice.dto.ActionResponseDto;
import ifortex.shuman.uladzislau.authservice.dto.MessageResponseDto;
import ifortex.shuman.uladzislau.authservice.model.Permissions;
import ifortex.shuman.uladzislau.authservice.paramedic.dto.KycApplicationDetailsDto;
import ifortex.shuman.uladzislau.authservice.paramedic.dto.KycApplicationSummaryDto;
import ifortex.shuman.uladzislau.authservice.paramedic.dto.KycRejectionRequestDto;
import ifortex.shuman.uladzislau.authservice.paramedic.dto.UpdateKycStatusRequestDto;
import ifortex.shuman.uladzislau.authservice.paramedic.service.KycService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/kyc/applications")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('" + Permissions.ADMIN_KYC_READ + "')")
public class AdminKycController {

  private final KycService kycService;

  @GetMapping
  public ResponseEntity<Page<KycApplicationDetailsDto>> getPendingApplications(Pageable pageable) {
    return ResponseEntity.ok(kycService.getPendingApplications(pageable));
  }

  @GetMapping("/{applicationId}")
  public ResponseEntity<KycApplicationDetailsDto> getApplicationDetails(
      @PathVariable Long applicationId) {
    return ResponseEntity.ok(kycService.getApplicationDetails(applicationId));
  }

  @PatchMapping("/{applicationId}")
  @PreAuthorize("hasAuthority('" + Permissions.ADMIN_KYC_MANAGE + "')")
  public ResponseEntity<ActionResponseDto> updateApplicationStatus(
      @PathVariable Long applicationId,
      @Valid @RequestBody UpdateKycStatusRequestDto request) {
    return ResponseEntity.ok(kycService.updateApplicationStatus(applicationId, request));
  }
}