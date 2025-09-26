package ifortex.shuman.uladzislau.authservice.service.implemention;

import ifortex.shuman.uladzislau.authservice.config.properties.SecurityProperties;
import ifortex.shuman.uladzislau.authservice.model.TokenType;
import ifortex.shuman.uladzislau.authservice.model.User;
import ifortex.shuman.uladzislau.authservice.model.UserToken;
import ifortex.shuman.uladzislau.authservice.repository.UserTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_REFRESH_TOKEN;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_USER_TOKEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserTokenServiceTest {

    @Mock
    private UserTokenRepository userTokenRepository;
    @Mock
    private SecurityProperties securityProperties;

    @InjectMocks
    private UserTokenServiceImpl userTokenService;

    @Captor
    private ArgumentCaptor<UserToken> tokenCaptor;

    private User testUser;
    private UserToken testToken;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).build();
        testToken = UserToken.builder()
                .id(10L)
                .user(testUser)
                .token(TEST_USER_TOKEN)
                .type(TokenType.REFRESH)
                .expiryTime(Instant.now().plus(1, ChronoUnit.DAYS))
                .build();

        ReflectionTestUtils.setField(userTokenService, "refreshTokenExpiration", 604800000L);
    }

    @Test
    void save_WhenTokenIsValid_ShouldCallRepository() {
        userTokenService.save(testToken);

        verify(userTokenRepository, times(1)).save(testToken);
    }

    @Test
    void save_WhenTokenIsNull_ShouldThrowNullPointerException() {
        assertThatThrownBy(() -> userTokenService.save(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void findByTokenAndType_WhenTokenExists_ShouldReturnToken() {
        when(userTokenRepository.findByTokenAndType(TEST_USER_TOKEN, TokenType.REFRESH))
                .thenReturn(Optional.of(testToken));
        UserToken foundToken = userTokenService.findByTokenAndType(TEST_USER_TOKEN, TokenType.REFRESH);

        assertThat(foundToken).isEqualTo(testToken);
    }

    @Test
    void findByTokenAndType_WhenTokenNotFound_ShouldThrowException() {
        when(userTokenRepository.findByTokenAndType(anyString(), any(TokenType.class)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userTokenService
                .findByTokenAndType(TEST_USER_TOKEN, TokenType.REFRESH))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid password reset token.");
    }

    @Test
    void saveUserRefreshToken_ShouldDeleteOldAndSaveNewToken() {
        String refreshTokenString = TEST_REFRESH_TOKEN;

        userTokenService.saveUserRefreshToken(testUser, refreshTokenString);

        verify(userTokenRepository, times(1))
                .deleteAllByUserAndType(testUser, TokenType.REFRESH);
        verify(userTokenRepository, times(1)).save(tokenCaptor.capture());

        UserToken savedToken = tokenCaptor.getValue();
        assertThat(savedToken.getUser()).isEqualTo(testUser);
        assertThat(savedToken.getToken()).isEqualTo(refreshTokenString);
        assertThat(savedToken.getType()).isEqualTo(TokenType.REFRESH);
        assertThat(savedToken.getExpiryTime()).isAfter(Instant.now().plus(6, ChronoUnit.DAYS));
    }

    @Test
    void createPasswordResetToken_ShouldDeleteOldAndSaveNewToken() {
        Duration testExpiration = Duration.ofHours(1);
        when(securityProperties.getPasswordResetTokenExpiration()).thenReturn(testExpiration);
        String resetTokenString = userTokenService.createPasswordResetToken(testUser);

        assertThat(resetTokenString).isNotNull().isNotBlank();

        verify(userTokenRepository, times(1))
                .deleteAllByUserAndType(testUser, TokenType.PASSWORD_RESET);
        verify(userTokenRepository, times(1)).save(tokenCaptor.capture());

        UserToken savedToken = tokenCaptor.getValue();
        assertThat(savedToken.getUser()).isEqualTo(testUser);
        assertThat(savedToken.getToken()).isEqualTo(resetTokenString);
        assertThat(savedToken.getType()).isEqualTo(TokenType.PASSWORD_RESET);
        assertThat(savedToken.getExpiryTime()).isAfter(Instant.now().plus(59, ChronoUnit.MINUTES));
    }

    @Test
    void validateAndRetrieveToken_WithValidToken_ShouldReturnToken() {
        UserTokenServiceImpl spyUserTokenService = spy(userTokenService);

        doReturn(testToken).when(spyUserTokenService).findByTokenAndType(anyString(), any(TokenType.class));

        UserToken result = spyUserTokenService.validateAndRetrieveToken(TEST_USER_TOKEN, TokenType.REFRESH);

        assertThat(result).isEqualTo(testToken);
        verify(spyUserTokenService, never()).delete(any());
    }

    @Test
    void validateAndRetrieveToken_WithExpiredToken_ShouldThrowExceptionAndDeleteToken() {
        testToken.setExpiryTime(Instant.now().minus(1, ChronoUnit.MINUTES));

        UserTokenServiceImpl spyUserTokenService = spy(userTokenService);
        doReturn(testToken).when(spyUserTokenService).findByTokenAndType(anyString(), any(TokenType.class));

        assertThatThrownBy(() -> spyUserTokenService
                .validateAndRetrieveToken(TEST_USER_TOKEN, TokenType.PASSWORD_RESET))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Token has expired.");

        verify(spyUserTokenService, times(1)).delete(testToken);
        verify(userTokenRepository, times(1)).delete(testToken);
    }
}