package ifortex.shuman.uladzislau.authservice.controller.frontend;

import ifortex.shuman.uladzislau.authservice.config.properties.FrontendProperties;
import ifortex.shuman.uladzislau.authservice.model.User;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
@Slf4j
public class ProfilePageController {

  private final FrontendProperties frontendProperties;

  @GetMapping("/link-google")
  public RedirectView linkGoogleAccount(HttpSession session) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String baseUrl = frontendProperties.getBaseUrl();

    if (authentication == null || !authentication.isAuthenticated()
        || !(authentication.getPrincipal() instanceof User)) {
      log.warn("Attempt to link Google account without authentication.");
      return new RedirectView(baseUrl + "/login");
    }

    User currentUser = (User) authentication.getPrincipal();
    log.info("User {} is initiating Google account linking. Storing userId {} in session.",
        currentUser.getEmail(), currentUser.getId());

    session.setAttribute("linkingUserId", currentUser.getId());

    String redirectUrl = baseUrl + "/oauth2/authorization/google";
    log.info("Redirecting to absolute OAuth2 URL: {}", redirectUrl);

    return new RedirectView(redirectUrl);
  }
}