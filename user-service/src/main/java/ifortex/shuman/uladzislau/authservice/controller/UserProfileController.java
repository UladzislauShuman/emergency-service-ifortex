package ifortex.shuman.uladzislau.authservice.controller;

import ifortex.shuman.uladzislau.authservice.dto.ChangePasswordRequestDto;
import ifortex.shuman.uladzislau.authservice.dto.EmailChangeConfirmDto;
import ifortex.shuman.uladzislau.authservice.dto.EmailChangeRequestDto;
import ifortex.shuman.uladzislau.authservice.dto.EmailChangeResponseDto;
import ifortex.shuman.uladzislau.authservice.dto.MessageResponseDto;
import ifortex.shuman.uladzislau.authservice.dto.UpdateProfileRequestDto;
import ifortex.shuman.uladzislau.authservice.dto.UserDto;
import ifortex.shuman.uladzislau.authservice.model.Permissions;
import ifortex.shuman.uladzislau.authservice.model.User;
import ifortex.shuman.uladzislau.authservice.service.ProfileUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Slf4j
public class UserProfileController {

  private final ProfileUserService profileUserService;

  @GetMapping
  public ResponseEntity<UserDto> getUserById(@AuthenticationPrincipal User currentUser) {
    return ResponseEntity.ok(profileUserService.getUserProfile(currentUser.getId()));
  }

  @PatchMapping
  public ResponseEntity<UserDto> updateUserProfile(
      @AuthenticationPrincipal User currentUser,
      @Valid @RequestBody UpdateProfileRequestDto request
  ) {
    return ResponseEntity.ok(profileUserService.updateProfile(currentUser.getId(), request));
  }

  @PostMapping("/change-password")
  public ResponseEntity<MessageResponseDto> changePassword(
      @AuthenticationPrincipal User currentUser,
      @Valid @RequestBody ChangePasswordRequestDto request) {
    return ResponseEntity.ok(profileUserService.changePassword(currentUser, request));
  }

  @PostMapping("/2fa")
  public ResponseEntity<MessageResponseDto> set2FA(
      @AuthenticationPrincipal User currentUser,
      @RequestParam boolean enable) {
    return ResponseEntity.ok(profileUserService.setTwoFactorAuthentication(currentUser, enable));
  }

  @PostMapping("/email-change-request")
  public ResponseEntity<MessageResponseDto> requestEmailChange(
      @AuthenticationPrincipal User currentUser,
      @Valid @RequestBody EmailChangeRequestDto request) {
    return ResponseEntity.ok(profileUserService.requestEmailChange(currentUser, request));
  }

  @PostMapping("/email-change-confirm")
  public ResponseEntity<EmailChangeResponseDto> confirmEmailChange(
      @AuthenticationPrincipal User currentUser,
      @Valid @RequestBody EmailChangeConfirmDto request) {
    return ResponseEntity.ok(profileUserService.confirmEmailChange(currentUser, request));
  }
}