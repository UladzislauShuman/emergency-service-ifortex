package ifortex.shuman.uladzislau.authservice.controller;

import ifortex.shuman.uladzislau.authservice.dto.CreateUserByAdminRequestDto;
import ifortex.shuman.uladzislau.authservice.dto.CreateUserResponseDto;
import ifortex.shuman.uladzislau.authservice.model.Permissions;
import ifortex.shuman.uladzislau.authservice.model.UserRole;
import ifortex.shuman.uladzislau.authservice.service.UserCreationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserCreationController {

  private final UserCreationService userCreationService;

  @PostMapping("/client")
  @PreAuthorize("hasAuthority('" + Permissions.ADMIN_CREATE_CLIENT + "')")
  public ResponseEntity<CreateUserResponseDto> createClient(
      @Valid @RequestBody CreateUserByAdminRequestDto request) {
    return createResponse(userCreationService.createUser(request, UserRole.ROLE_CLIENT));
  }

  @PostMapping("/paramedic")
  @PreAuthorize("hasAuthority('" + Permissions.ADMIN_CREATE_PARAMEDIC + "')")
  public ResponseEntity<CreateUserResponseDto> createParamedic(
      @Valid @RequestBody CreateUserByAdminRequestDto request) {
    return createResponse(userCreationService.createUser(request, UserRole.ROLE_PARAMEDIC));
  }

  @PostMapping("/admin")
  @PreAuthorize("hasAuthority('" + Permissions.ADMIN_CREATE_ADMIN + "')")
  public ResponseEntity<CreateUserResponseDto> createAdmin(
      @Valid @RequestBody CreateUserByAdminRequestDto request) {
    return createResponse(userCreationService.createUser(request, UserRole.ROLE_ADMIN));
  }

  @PostMapping("/super-admin")
  @PreAuthorize("hasAuthority('" + Permissions.ADMIN_CREATE_SUPER_ADMIN + "')")
  public ResponseEntity<CreateUserResponseDto> createSuperAdmin(
      @Valid @RequestBody CreateUserByAdminRequestDto request) {
    return createResponse(userCreationService.createUser(request, UserRole.ROLE_SUPER_ADMIN));
  }

  private ResponseEntity<CreateUserResponseDto> createResponse(CreateUserResponseDto responseDto) {
    return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
  }
}