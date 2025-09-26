package ifortex.shuman.uladzislau.authservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import ifortex.shuman.uladzislau.authservice.config.properties.FrontendProperties;
import ifortex.shuman.uladzislau.authservice.exception.UserNotFoundException;
import ifortex.shuman.uladzislau.authservice.model.User;

import ifortex.shuman.uladzislau.authservice.service.JwtService;
import ifortex.shuman.uladzislau.authservice.service.ProfileUserService;
import ifortex.shuman.uladzislau.authservice.service.UserService;
import ifortex.shuman.uladzislau.authservice.service.UserTokenService;
import ifortex.shuman.uladzislau.authservice.util.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

  private final UserService userService;
  private final ProfileUserService profileUserService;
  private final JwtService jwtService;
  private final UserTokenService userTokenService;
  private final UserMapper userMapper;
  private final ObjectMapper objectMapper;
  private final FrontendProperties frontendProperties;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication
  ) throws IOException {

    OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
    HttpSession session = request.getSession(false);
    Long linkingUserId = (session != null) ? (Long) session.getAttribute("linkingUserId") : null;

    if (linkingUserId != null) {
      session.removeAttribute("linkingUserId");
      handleAccountLinking(request, response, oauth2User, linkingUserId);
    } else {
      handleOAuth2SignIn(response, oauth2User);
    }
  }

  private void handleAccountLinking(
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2User oauth2User,
      Long linkingUserId)
      throws IOException {
    String googleId = oauth2User.getName();
    String email = oauth2User.getAttribute("email");
    log.info("OAuth2 flow: Linking account for user ID: {}", linkingUserId);

    try {
      profileUserService.linkGoogleAccount(linkingUserId, googleId, email);
      String redirectUrl = frontendProperties.getBaseUrl() + "/home";
      log.info("Account linking successful. Redirecting to {}", redirectUrl);
      response.sendRedirect(redirectUrl);
    } catch (Exception e) {
      log.error("Failed to link Google account for user {}", linkingUserId, e);

      HttpSession session = request.getSession(false);
      if (session != null) {
        session.invalidate();
        log.info("HttpSession invalidated due to account linking error.");
      }

      String encodedMessage = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
      String errorRedirectUrl = frontendProperties.getBaseUrl() + "/home?error=" + encodedMessage;
      response.sendRedirect(errorRedirectUrl);
    }
  }

  private void handleOAuth2SignIn(HttpServletResponse response, OAuth2User oauth2User)
      throws IOException {
    String googleId = oauth2User.getName();
    log.info("OAuth2 flow: Sign-in attempt for Google ID: {}", googleId);

    try {
      User user = userService.findByGoogleId(googleId);
      String accessToken = jwtService.generateToken(user);
      String refreshToken = jwtService.generateRefreshToken(user);
      userTokenService.saveUserRefreshToken(user, refreshToken);

      String userDtoJson = objectMapper.writeValueAsString(userMapper.toUserDto(user));

      String baseUrl = frontendProperties.getBaseUrl();
      String redirectUrl = String.format(
          "%s/login/oauth2/success?accessToken=%s&refreshToken=%s&user=%s",
          baseUrl,
          URLEncoder.encode(accessToken, StandardCharsets.UTF_8),
          URLEncoder.encode(refreshToken, StandardCharsets.UTF_8),
          URLEncoder.encode(userDtoJson, StandardCharsets.UTF_8)
      );
      log.info("OAuth2 sign-in successful. Redirecting to {}", redirectUrl);
      response.sendRedirect(redirectUrl);

    } catch (UserNotFoundException e) {
      log.warn("OAuth2 sign-in failed: No user found with Google ID {}. User must register first.",
          googleId);
      String message = URLEncoder.encode(
          "This Google account is not linked to any user. Please register and link your account first.",
          StandardCharsets.UTF_8);
      String errorRedirectUrl = frontendProperties.getBaseUrl() + "/login?error=" + message;
      response.sendRedirect(errorRedirectUrl);
    }
  }
}