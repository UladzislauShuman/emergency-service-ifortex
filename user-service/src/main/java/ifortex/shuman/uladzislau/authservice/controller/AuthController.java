package ifortex.shuman.uladzislau.authservice.controller;

import ifortex.shuman.uladzislau.authservice.dto.AdminPasswordResetConfirmDto;
import ifortex.shuman.uladzislau.authservice.dto.JwtTokenDto;
import ifortex.shuman.uladzislau.authservice.dto.LoginRequestDto;
import ifortex.shuman.uladzislau.authservice.dto.LoginResponseDto;
import ifortex.shuman.uladzislau.authservice.dto.MessageResponseDto;
import ifortex.shuman.uladzislau.authservice.dto.PasswordResetConfirmDto;
import ifortex.shuman.uladzislau.authservice.dto.PasswordResetRequestDto;
import ifortex.shuman.uladzislau.authservice.dto.RefreshTokenRequestDto;
import ifortex.shuman.uladzislau.authservice.dto.RegisterRequestDto;
import ifortex.shuman.uladzislau.authservice.dto.ResendOtpRequestDto;
import ifortex.shuman.uladzislau.authservice.dto.VerificationRequestDto;
import ifortex.shuman.uladzislau.authservice.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthenticationService authenticationService;

  @PostMapping("/registration")
  public ResponseEntity<MessageResponseDto> register(
      @Valid @RequestBody RegisterRequestDto request) {
    return ResponseEntity.ok(authenticationService.register(request));
  }

  @PostMapping("/verification/email")
  public ResponseEntity<MessageResponseDto> verifyEmail(
      @Valid @RequestBody VerificationRequestDto request) {
    return ResponseEntity.ok(authenticationService.verifyEmail(request));
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
    return ResponseEntity.ok(authenticationService.login(request));
  }

  @PostMapping("/verification/2fa")
  public ResponseEntity<LoginResponseDto> verify(
      @Valid @RequestBody VerificationRequestDto request) {
    return ResponseEntity.ok(authenticationService.verify2FA(request));
  }

  @PostMapping("/refresh")
  public ResponseEntity<JwtTokenDto> refreshToken(
      @Valid @RequestBody RefreshTokenRequestDto request) {
    return ResponseEntity.ok(authenticationService.refreshAccessToken(request));
  }

  @PostMapping("/password-request-reset")
  public ResponseEntity<MessageResponseDto> requestPasswordReset(
      @Valid @RequestBody PasswordResetRequestDto request) {
    return ResponseEntity.ok(authenticationService.requestPasswordReset(request.getEmail()));
  }

  @PatchMapping("/password-request-reset")
  public ResponseEntity<MessageResponseDto> confirmPasswordReset(
      @Valid @RequestBody PasswordResetConfirmDto request) {
    return ResponseEntity.ok(authenticationService.confirmPasswordReset(request));
  }

  @PostMapping("/password-admin-link")
  public ResponseEntity<MessageResponseDto> confirmAdminPasswordReset(
      @Valid @RequestBody AdminPasswordResetConfirmDto request) {
    return ResponseEntity.ok(authenticationService.confirmAdminPasswordReset(request));
  }

  @PostMapping("/otp/resend")
  public ResponseEntity<MessageResponseDto> resendOtp(
      @Valid @RequestBody ResendOtpRequestDto request) {
    return ResponseEntity.ok(authenticationService.resendOtp(request));
  }

  @PostMapping("/logout")
  public ResponseEntity<MessageResponseDto> logout(HttpServletRequest request) {
    return ResponseEntity.ok(authenticationService.logout(request));
  }
}
