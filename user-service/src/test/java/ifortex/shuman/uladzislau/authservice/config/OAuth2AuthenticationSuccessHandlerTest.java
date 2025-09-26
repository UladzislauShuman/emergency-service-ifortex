package ifortex.shuman.uladzislau.authservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import ifortex.shuman.uladzislau.authservice.exception.UserNotFoundException;
import ifortex.shuman.uladzislau.authservice.model.User;
import ifortex.shuman.uladzislau.authservice.service.implemention.JwtServiceImpl;
import ifortex.shuman.uladzislau.authservice.service.implemention.ProfileUserServiceImpl;
import ifortex.shuman.uladzislau.authservice.service.implemention.UserServiceImpl;
import ifortex.shuman.uladzislau.authservice.service.implemention.UserTokenServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static ifortex.shuman.uladzislau.authservice.TestConstanst.EMAIL_ATTRIBUTE;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.LINKING_USER_ID_ATTRIBUTE;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_ACCESS_TOKEN;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_EMAIL;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_GOOGLE_ID;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_REFRESH_TOKEN;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import java.io.IOException;
import java.io.PrintWriter;


@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationSuccessHandlerTest {

    @Mock
    private UserServiceImpl userService;
    @Mock
    private ProfileUserServiceImpl profileUserService;
    @Mock
    private JwtServiceImpl jwtServiceImpl;
    @Mock
    private UserTokenServiceImpl userTokenService;
    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private Authentication authentication;
    @Mock
    private OAuth2User oauth2User;
    @Mock
    private HttpSession session;
    @Mock
    private PrintWriter writer;

    @InjectMocks
    private OAuth2AuthenticationSuccessHandler successHandler;

    @BeforeEach
    void setUp() throws IOException {
        when(response.getWriter()).thenReturn(writer);
        when(request.getSession(false)).thenReturn(session);
        when(authentication.getPrincipal()).thenReturn(oauth2User);
        when(oauth2User.getName()).thenReturn(TEST_GOOGLE_ID);
    }

    @Test
    void onAuthenticationSuccess_ForAccountLinking_ShouldCallLinker() throws IOException {
        when(session.getAttribute(LINKING_USER_ID_ATTRIBUTE)).thenReturn(1L);
        when(oauth2User.getAttribute(EMAIL_ATTRIBUTE)).thenReturn(TEST_EMAIL);

        successHandler.onAuthenticationSuccess(request, response, authentication);

        verify(profileUserService).linkGoogleAccount(1L, TEST_GOOGLE_ID, TEST_EMAIL);
        verify(session).removeAttribute(LINKING_USER_ID_ATTRIBUTE);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(objectMapper).writeValue(eq(writer), any());
    }

    @Test
    void onAuthenticationSuccess_ForSignIn_WhenUserExists_ShouldGenerateTokens() throws IOException {
        when(session.getAttribute(LINKING_USER_ID_ATTRIBUTE)).thenReturn(null);
        User user = User.builder().build();
        when(userService.findByGoogleId(TEST_GOOGLE_ID)).thenReturn(user);
        when(jwtServiceImpl.generateToken(user)).thenReturn(TEST_ACCESS_TOKEN);
        when(jwtServiceImpl.generateRefreshToken(user)).thenReturn(TEST_REFRESH_TOKEN);

        successHandler.onAuthenticationSuccess(request, response, authentication);

        verify(jwtServiceImpl).generateToken(user);
        verify(userTokenService).saveUserRefreshToken(user, TEST_REFRESH_TOKEN);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(objectMapper).writeValue(eq(writer), any());
    }

    @Test
    void onAuthenticationSuccess_ForSignIn_WhenUserNotFound_ShouldReturnUnauthorized() throws IOException {
        when(session.getAttribute(LINKING_USER_ID_ATTRIBUTE)).thenReturn(null);
        when(userService.findByGoogleId(anyString())).thenThrow(new UserNotFoundException(""));

        successHandler.onAuthenticationSuccess(request, response, authentication);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(objectMapper).writeValue(eq(writer), any());
        verify(jwtServiceImpl, never()).generateToken(any());
    }
}