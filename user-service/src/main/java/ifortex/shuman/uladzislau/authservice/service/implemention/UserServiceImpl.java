package ifortex.shuman.uladzislau.authservice.service.implemention;

import ifortex.shuman.uladzislau.authservice.exception.UserNotFoundException;
import ifortex.shuman.uladzislau.authservice.model.TokenType;
import ifortex.shuman.uladzislau.authservice.model.User;
import ifortex.shuman.uladzislau.authservice.model.UserStatus;
import ifortex.shuman.uladzislau.authservice.repository.UserRepository;
import ifortex.shuman.uladzislau.authservice.service.UserService;
import ifortex.shuman.uladzislau.authservice.service.UserTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;

import static ifortex.shuman.uladzislau.authservice.config.Constants.VALID_STATUSES_FOR_LOGIN;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserDetailsService, UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserTokenService userTokenService;

  @Value("${application.security.temp-password.expiration-hours}")
  private long tempPasswordExpirationHours;

  @Override
  public User getByEmail(String email) {
    return userRepository.findByEmailAndStatusIn(email, VALID_STATUSES_FOR_LOGIN)
        .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
  }

  @Override
  public UserDetails loadUserByUsername(String username) {
    User user = getByEmail(username);

    if (user.getBlockedUntil() != null && user.getBlockedUntil().isBefore(Instant.now())) {
      log.info("User {} temporary block has expired. Unlocking automatically.", user.getEmail());
      user.setBlockedUntil(null);
      return save(user);
    }
    return user;
  }

  @Override
  public String generateRandomPassword() {
    return "Temp" + new Random().nextInt(99999) + "!";
  }

  @Override
  public User findByGoogleId(String googleId) {
    Objects.requireNonNull(googleId, "googleId is null");
    return userRepository.findByGoogleId(googleId)
        .orElseThrow(() -> new UserNotFoundException("User not found with Google ID: " + googleId));
  }

  @Override
  public User findById(Long userId) {
    Objects.requireNonNull(userId, "userId is null");
    return userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User not found with Id:" + userId));
  }

  @Override
  public User save(User user) {
    Objects.requireNonNull(user, "User is null");
    return userRepository.save(user);
  }

  @Override
  public String setUserTemporaryPassword(Long userId) {
    String tempPassword = generateRandomPassword();
    performUserAction(userId, user -> {
      user.setPassword(passwordEncoder.encode(tempPassword));
      user.setPasswordTemporary(true);
      user.setPasswordExpiryTime(Instant.now().plus(tempPasswordExpirationHours, ChronoUnit.HOURS));
    });
    return tempPassword;
  }

  @Override
  public void updateUserPassword(Long userId, String newRawPassword) {
    performUserAction(userId, user -> {
      updateUserPasswordTemporaryFlagAndExpireTime(user, newRawPassword);
    });
  }

  @Override
  public void updateUserEmailAndInvalidateTokens(Long userId, String newEmail) {
    performUserAction(userId, user -> {
      user.setEmail(newEmail);
    });
  }

  @Override
  public void updateUserPasswordAndStatus(Long userId, String newRawPassword,
      UserStatus newStatus) {
    performUserAction(userId, user -> {
      updateUserPasswordTemporaryFlagAndExpireTime(user, newRawPassword);
      user.setStatus(newStatus);
    });
  }

  @Override
  public Optional<User> findUserByEmailAndStatus(String email, UserStatus status) {
    return userRepository.findByEmailAndStatus(email, status);
  }

  @Override
  public void softDeleteUser(Long userId) {
    performUserAction(userId, user -> {
      log.info("Soft deleting user {}. Invalidating all sessions.", user.getEmail());
      user.setStatus(UserStatus.DELETED);
      user.setGoogleId(null);
      userTokenService.deleteAllByUserAndType(user, TokenType.REFRESH);
    });
  }

  @Override
  public void hardDeleteUser(Long userId) {
    User user = findById(userId);
    userRepository.delete(user);
  }

  private void performUserAction(Long userId, Consumer<User> action) {
    User user = findById(userId);
    action.accept(user);
    save(user);
  }

  private void updateUserPasswordTemporaryFlagAndExpireTime(User user, String newRawPassword) {
    user.setPassword(passwordEncoder.encode(newRawPassword));
    user.setPasswordTemporary(false);
    user.setPasswordExpiryTime(null);
  }
}
