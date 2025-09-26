package ifortex.shuman.uladzislau.authservice.paramedic.controller;

import ifortex.shuman.uladzislau.authservice.dto.MessageResponseDto;
import ifortex.shuman.uladzislau.authservice.dto.VerificationRequestDto;
import ifortex.shuman.uladzislau.authservice.paramedic.dto.KycStatusResponseDto;
import ifortex.shuman.uladzislau.authservice.paramedic.dto.KycSubmissionRequestDto;
import ifortex.shuman.uladzislau.authservice.paramedic.service.KycService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/kyc/applications")
@RequiredArgsConstructor
public class KycController {

  private final KycService kycService;

  @PostMapping
  public ResponseEntity<MessageResponseDto> submitKycApplication(
      @Valid @ModelAttribute KycSubmissionRequestDto request) {
    return ResponseEntity.ok(kycService.submitApplication(request));
  }

  @PatchMapping("/verification")
  public ResponseEntity<MessageResponseDto> verifyEmail(
      @Valid @RequestBody VerificationRequestDto request) {
    return ResponseEntity.ok(kycService.verifyApplicationEmail(request));
  }

  @GetMapping
  public ResponseEntity<KycStatusResponseDto> getKycStatus(@RequestParam @Email String email) {
    return ResponseEntity.ok(kycService.getKycStatus(email));
  }
}