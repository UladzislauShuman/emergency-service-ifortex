package ifortex.shuman.uladzislau.authservice.controller;

import ifortex.shuman.uladzislau.authservice.dto.BlockUserRequestDto;
import ifortex.shuman.uladzislau.authservice.dto.MessageResponseDto;
import ifortex.shuman.uladzislau.authservice.dto.UpdateProfileRequestDto;
import ifortex.shuman.uladzislau.authservice.dto.UserDto;
import ifortex.shuman.uladzislau.authservice.dto.UserSearchRequestDto;
import ifortex.shuman.uladzislau.authservice.model.Permissions;
import ifortex.shuman.uladzislau.authservice.model.User;
import ifortex.shuman.uladzislau.authservice.service.AdminUserService;
import ifortex.shuman.uladzislau.authservice.service.ProfileUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final AdminUserService adminUserService;
  private final ProfileUserService profileUserService;

  @GetMapping("/{userId}")
  @PreAuthorize("hasAuthority('" + Permissions.ADMIN_USER_READ + "') or #userId == principal.id")
  public ResponseEntity<UserDto> getUserById(@PathVariable Long userId) {
    return ResponseEntity.ok(profileUserService.getUserProfile(userId));
  }

  @PatchMapping("/{userId}")
  @PreAuthorize("hasAuthority('" + Permissions.ADMIN_USER_UPDATE + "') or #userId == principal.id")
  public ResponseEntity<UserDto> updateUserProfile(
      @PathVariable Long userId,
      @Valid @RequestBody UpdateProfileRequestDto request
  ) {
    return ResponseEntity.ok(profileUserService.updateProfile(userId, request));
  }

  // Deleting
  @PostMapping("/{userId}/deletion-request")
  @PreAuthorize("#userId == principal.id")
  public ResponseEntity<MessageResponseDto> requestAccountDeletion(@PathVariable Long userId) {
    return ResponseEntity.ok(profileUserService.requestAccountDeletion(userId));
  }

  @DeleteMapping("/{userId}/deletion-request")
  @PreAuthorize("hasAuthority('" + Permissions.ADMIN_USER_UPDATE + "')")
  public ResponseEntity<UserDto> cancelAccountDeletion(@PathVariable Long userId) {
    return ResponseEntity.ok(adminUserService.cancelAccountDeletion(userId));
  }

  @DeleteMapping("/{userId}/soft")
  @PreAuthorize("hasAuthority('" + Permissions.ADMIN_USER_DELETE_SOFT + "')")
  public ResponseEntity<MessageResponseDto> softDeleteUser(
      @PathVariable Long userId,
      @AuthenticationPrincipal User currentUser) {
    return ResponseEntity.ok(adminUserService.softDeleteUser(userId, currentUser));
  }

  @DeleteMapping("/{userId}/hard")
  @PreAuthorize("hasAuthority('" + Permissions.ADMIN_USER_DELETE_HARD + "')")
  public ResponseEntity<MessageResponseDto> hardDeleteUser(
      @PathVariable Long userId,
      @AuthenticationPrincipal User currentUser) {
    return ResponseEntity.ok(adminUserService.hardDeleteUser(userId, currentUser));
  }


  @PostMapping("/search")
  @PreAuthorize("hasAuthority('" + Permissions.ADMIN_USER_READ + "')")
  public ResponseEntity<Page<UserDto>> findUsersByCriteria(
      @RequestBody UserSearchRequestDto request,
      Pageable pageable
  ) {
    Page<UserDto> userDtoPage = adminUserService
        .findUsersByComplexFilter(request, pageable);
    return ResponseEntity.ok(userDtoPage);
  }

  @PutMapping("/{userId}/lock")
  @PreAuthorize("hasAuthority('" + Permissions.ADMIN_USER_BLOCK + "')")
  public ResponseEntity<UserDto> updateBlockStatus(
      @PathVariable Long userId,
      @AuthenticationPrincipal User currentUser,
      @Valid @RequestBody BlockUserRequestDto request
  ) {
    return ResponseEntity.ok(adminUserService.updateBlockStatus(currentUser, userId, request));
  }
}
