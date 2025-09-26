package ifortex.shuman.uladzislau.authservice.controller;

import ifortex.shuman.uladzislau.authservice.dto.AdminPasswordResetResponseDto;
import ifortex.shuman.uladzislau.authservice.model.Permissions;
import ifortex.shuman.uladzislau.authservice.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/actions")
@RequiredArgsConstructor
public class UserActionController {

  private final AdminUserService adminUserService;

  @PostMapping("/{userId}/send-reset-link")
  @PreAuthorize("hasAuthority('" + Permissions.ADMIN_USER_RESET_PASSWORD_RESET_LINK + "')")
  public ResponseEntity<AdminPasswordResetResponseDto> resetPasswordByLink(
      @PathVariable Long userId) {
    AdminPasswordResetResponseDto response = adminUserService.sendPasswordResetLink(userId);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{userId}/generate-temp-password")
  @PreAuthorize("hasAuthority('" + Permissions.ADMIN_USER_RESET_PASSWORD_GENERATE_TEMP + "')")
  public ResponseEntity<AdminPasswordResetResponseDto> resetPasswordByGenerating(
      @PathVariable Long userId) {
    AdminPasswordResetResponseDto response = adminUserService.generateTemporaryPassword(userId);
    return ResponseEntity.ok(response);
  }
}