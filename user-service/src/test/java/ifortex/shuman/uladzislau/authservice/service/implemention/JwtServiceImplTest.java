package ifortex.shuman.uladzislau.authservice.service.implemention;

import ifortex.shuman.uladzislau.authservice.config.properties.JwtProperties;
import ifortex.shuman.uladzislau.authservice.service.permission.PermissionService;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import ifortex.shuman.uladzislau.authservice.model.User;

import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_EMAIL;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_OTHER_EMAIL;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_SECRET_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class JwtServiceImplTest {

  public static final int ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 5;
  public static final int REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 30;

  private JwtServiceImpl jwtServiceImpl;
  private PermissionService permissionService;
  private User user;

  @BeforeEach
  void setUp() {
    JwtProperties testJwtProperties = new JwtProperties();
    testJwtProperties.setSecretKey(TEST_SECRET_KEY);

    JwtProperties.AccessToken accessTokenProps = new JwtProperties.AccessToken();
    accessTokenProps.setExpiration(ACCESS_TOKEN_EXPIRATION);
    testJwtProperties.setAccessToken(accessTokenProps);

    JwtProperties.RefreshToken refreshTokenProps = new JwtProperties.RefreshToken();
    refreshTokenProps.setExpiration(REFRESH_TOKEN_EXPIRATION);
    testJwtProperties.setRefreshToken(refreshTokenProps);

    permissionService = Mockito.mock(PermissionService.class);
    jwtServiceImpl = new JwtServiceImpl(testJwtProperties, permissionService);

    user = Mockito.mock(User.class);
    when(user.getId()).thenReturn(1L);
    when(user.getUsername()).thenReturn(TEST_EMAIL);
    Set<String> permissions = Set.of("user:read", "report:export");
    when(permissionService.calculatePermissionsForUser(user)).thenReturn(permissions);
  }

  @Test
  void generateToken_ShouldCreateValidToken_And_extractUsername_ShouldReturnCorrectUsername() {
    String token = jwtServiceImpl.generateToken(user);
    String extractedUsername = jwtServiceImpl.extractUsername(token);

    assertThat(token).isNotNull().isNotBlank();
    assertThat(extractedUsername).isEqualTo(TEST_EMAIL);
  }

  @Test
  void isTokenValid_WithValidTokenAndCorrectUser_ShouldReturnTrue() {
    String token = jwtServiceImpl.generateToken(user);

    boolean isValid = jwtServiceImpl.isTokenValid(token, user);

    assertThat(isValid).isTrue();
  }

  @Test
  void isTokenValid_WithValidTokenAndDifferentUser_ShouldReturnFalse() {
    String token = jwtServiceImpl.generateToken(user);

    UserDetails otherUserDetails = Mockito.mock(UserDetails.class);
    when(otherUserDetails.getUsername()).thenReturn(TEST_OTHER_EMAIL);

    boolean isValid = jwtServiceImpl.isTokenValid(token, otherUserDetails);

    assertThat(isValid).isFalse();
  }

  @Test
  void isTokenValid_WhenTokenIsExpired_ShouldReturnFalse() {
    JwtProperties expiredProps = new JwtProperties();
    expiredProps.setSecretKey(TEST_SECRET_KEY);
    JwtProperties.AccessToken accessTokenProps = new JwtProperties.AccessToken();
    accessTokenProps.setExpiration(-5000L);
    expiredProps.setAccessToken(accessTokenProps);

    JwtServiceImpl expiredJwtServiceImpl = new JwtServiceImpl(expiredProps, permissionService);

    String expiredToken = expiredJwtServiceImpl.generateToken(user);

    boolean isValid = jwtServiceImpl.isTokenValid(expiredToken, user);
    assertThat(isValid).isFalse();
  }


  @Test
  void extractClaim_ForAuthorities_ShouldReturnCorrectAuthorities() {
    String token = jwtServiceImpl.generateToken(user);

    List<String> extractedAuthorities = jwtServiceImpl.extractClaim(token, claims ->
        claims.get("authorities", List.class));

    assertThat(extractedAuthorities).isNotNull();
    assertThat(extractedAuthorities).hasSize(2);
    assertThat(extractedAuthorities).containsExactlyInAnyOrder("user:read", "report:export");
  }

  @Test
  void generateRefreshToken_ShouldCreateValidToken() {
    String refreshToken = jwtServiceImpl.generateRefreshToken(user);
    String extractedUsername = jwtServiceImpl.extractUsername(refreshToken);

    assertThat(refreshToken).isNotNull().isNotBlank();
    assertThat(extractedUsername).isEqualTo(TEST_EMAIL);

    boolean isValid = jwtServiceImpl.isTokenValid(refreshToken, user);
    assertThat(isValid).isTrue();
  }

  @Test
  void isTokenValid_WithTokenSignedByDifferentKey_ShouldReturnFalse() {
    JwtProperties evilProps = new JwtProperties();
    String evilSecretKey = "YW5vdGhlciBzZWNyZXQga2V5IGZvciB0ZXN0aW5nIHB1cnBvc2VzIG11c3QgYmUgbG9uZw==";
    evilProps.setSecretKey(evilSecretKey);
    JwtProperties.AccessToken evilTokenProps = new JwtProperties.AccessToken();
    evilTokenProps.setExpiration(ACCESS_TOKEN_EXPIRATION);
    evilProps.setAccessToken(evilTokenProps);

    JwtServiceImpl evilJwtServiceImpl = new JwtServiceImpl(evilProps, permissionService);

    String invalidSignatureToken = evilJwtServiceImpl.generateToken(user);

    boolean isValid = jwtServiceImpl.isTokenValid(invalidSignatureToken, user);

    assertThat(isValid).isFalse();
  }
}