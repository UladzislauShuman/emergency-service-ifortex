package ifortex.shuman.uladzislau.authservice.service.validation;


import ifortex.shuman.uladzislau.authservice.exception.EmailAlreadyExistsException;
import ifortex.shuman.uladzislau.authservice.exception.OperationForbiddenException;
import ifortex.shuman.uladzislau.authservice.exception.ResourceConflictException;
import ifortex.shuman.uladzislau.authservice.model.User;
import ifortex.shuman.uladzislau.authservice.model.UserRole;
import ifortex.shuman.uladzislau.authservice.model.UserStatus;
import ifortex.shuman.uladzislau.authservice.repository.UserRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserValidator {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public void validateEmailIsAvailable(String email) {
    if (userRepository.existsByEmail(email)) {
      throw new EmailAlreadyExistsException("User with email " + email + " already exists.");
    }
  }

  public void ensureIsNotBlockingOrDeleteHimSelf(User currentUser, Long otherUserId) {
    if (currentUser.getId().equals(otherUserId)) {
      throw new OperationForbiddenException("Admins cannot block or delete themselves.");
    }
  }

  public void ensureNotSuperAdmin(User user) {
    if (user.getRole() != null && user.getRole().getName() == UserRole.ROLE_SUPER_ADMIN) {
      throw new OperationForbiddenException("Cannot do this with a super admin account.");
    }
  }

  public void validatePasswordConfirmation(String password, String confirmation) {
    if (password == null || !password.equals(confirmation)) {
      throw new IllegalArgumentException("Passwords do not match.");
    }
  }

  public void checkPasswordMatch(User user, String rawPassword) {
    if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
      throw new BadCredentialsException("The provided current password is not correct.");
    }
  }

  public void ensureUserIsActive(User user) {
    boolean isTemporarilyLocked =
        user.getBlockedUntil() != null && user.getBlockedUntil().isAfter(Instant.now());
    if (user.getStatus() != UserStatus.ACTIVE || user.isPermanentlyBlocked()
        || isTemporarilyLocked) {
      throw new OperationForbiddenException(
          String.format("Operation cannot be performed. User '%s' is not active.",
              user.getEmail()));
    }
  }

  public void ensureUserHasAdminRights(User user) {
    if (!(user.getRole().getName().equals(UserRole.ROLE_ADMIN) || user.getRole().getName()
        .equals(UserRole.ROLE_SUPER_ADMIN))) {
      throw new AccessDeniedException(
          String.format("User with id %d does not have admin rights", user.getId()));
    }
  }

  public void canBlockOrDeleteUser(User whoBlocking, User toBlocking) {
    ensureIsNotBlockingOrDeleteHimSelf(whoBlocking, toBlocking.getId());
    ensureUserHasAdminRights(whoBlocking);
    ensureUserIsActive(whoBlocking);
    if (whoBlocking.getRole().getName().equals(UserRole.ROLE_ADMIN) &&
        (toBlocking.getRole().getName().equals(UserRole.ROLE_ADMIN) ||
            toBlocking.getRole().getName().equals(UserRole.ROLE_SUPER_ADMIN))) {
      throw new AccessDeniedException(String.format("User with id %d has admin rights, " +
              "but he cant block or delete user %d, because he is admin or super admin",
          whoBlocking.getId(), toBlocking.getId()));
    }
  }

  public void validateDeletionPrivileges(User currentUser, User userToDelete) {
    ensureIsNotBlockingOrDeleteHimSelf(currentUser, userToDelete.getId());

    UserRole creatorRole = currentUser.getRole().getName();
    UserRole targetRole = userToDelete.getRole().getName();

    if (creatorRole == UserRole.ROLE_ADMIN) {
      if (targetRole == UserRole.ROLE_ADMIN || targetRole == UserRole.ROLE_SUPER_ADMIN) {
        log.warn("ACCESS DENIED: Admin {} attempted to delete another admin/super-admin {}",
            currentUser.getEmail(), userToDelete.getEmail());
        throw new AccessDeniedException("Admins cannot delete other admins or super admins.");
      }
    }
  }

  public void ensureNewPasswordIsDifferent(User user, String newRawPassword) {
    if (passwordEncoder.matches(newRawPassword, user.getPassword())) {
      throw new ResourceConflictException(
          "The new password cannot be the same as the old password.");
    }
  }
}
