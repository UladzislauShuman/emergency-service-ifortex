package ifortex.shuman.uladzislau.authservice.service.implemention;

import ifortex.shuman.uladzislau.authservice.config.properties.SecurityProperties;
import ifortex.shuman.uladzislau.authservice.model.TokenType;
import ifortex.shuman.uladzislau.authservice.model.User;
import ifortex.shuman.uladzislau.authservice.model.UserToken;
import ifortex.shuman.uladzislau.authservice.repository.UserTokenRepository;
import ifortex.shuman.uladzislau.authservice.service.UserTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserTokenServiceImpl implements UserTokenService {

  private final UserTokenRepository userTokenRepository;

  @Value("${application.security.jwt.refresh-token.expiration}")
  private long refreshTokenExpiration;

  private final SecurityProperties securityProperties;

  @Override
  public UserToken save(UserToken userToken) {
    validateUserToken(userToken);
    return userTokenRepository.save(userToken);
  }

  @Override
  public void delete(UserToken userToken) {
    validateUserToken(userToken);
    userTokenRepository.delete(userToken);
  }

  @Override
  public void deleteAllByUserAndType(User user, TokenType type) {
    Objects.requireNonNull(user, "User cannot be null");
    validateTokenType(type);
    userTokenRepository.deleteAllByUserAndType(user, type);
  }

  @Override
  public UserToken findByTokenAndType(String token, TokenType type) {
    Objects.requireNonNull(token, "Token string cannot be null");
    validateTokenType(type);
    return userTokenRepository.findByTokenAndType(token, type)
        .orElseThrow(() -> new IllegalArgumentException("Invalid password reset token."));
  }

  @Override
  @Transactional
  public void saveUserRefreshToken(User user, String token) {
    long expirationSeconds = refreshTokenExpiration / 1000;
    createAndSaveToken(user, token, TokenType.REFRESH,
        Instant.now().plus(expirationSeconds, ChronoUnit.SECONDS));
  }

  @Override
  @Transactional
  public String createPasswordResetToken(User user) {
    String token = UUID.randomUUID().toString();
    Instant expiryTime = Instant.now().plus(securityProperties.getPasswordResetTokenExpiration());
    createAndSaveToken(user, token, TokenType.PASSWORD_RESET, expiryTime);
    return token;
  }

  @Override
  public UserToken validateAndRetrieveToken(String token, TokenType type) {
    UserToken userToken = findByTokenAndType(token, type);
    if (isTokenExpire(userToken)) {
      delete(userToken);
      throw new IllegalArgumentException("Token has expired.");
    }

    return userToken;
  }

  @Override
  public boolean hasActiveRefreshToken(User user) {
    return userTokenRepository.existsByUserAndType(user, TokenType.REFRESH);
  }

  private boolean isTokenExpire(UserToken userToken) {
    return userToken.getExpiryTime().isBefore(Instant.now());
  }

  private void createAndSaveToken(User user, String token, TokenType type, Instant expiryTime) {
    deleteAllByUserAndType(user, type);

    UserToken newToken = UserToken.builder()
        .user(user)
        .token(token)
        .type(type)
        .expiryTime(expiryTime)
        .build();

    save(newToken);
  }

  private void validateUserToken(UserToken userToken) {
    Objects.requireNonNull(userToken, "UserToken cannot be null");
  }

  private void validateTokenType(TokenType type) {
    Objects.requireNonNull(type, "TokenType cannot be null");
  }
}
