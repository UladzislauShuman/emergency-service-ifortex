package ifortex.shuman.uladzislau.authservice.controller;

import ifortex.shuman.uladzislau.authservice.dto.MessageResponseDto;
import ifortex.shuman.uladzislau.authservice.model.User;
import jakarta.servlet.http.HttpSession;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
@Slf4j
public class OAuthController {

  private final StringRedisTemplate redisTemplate;

  @Value("${spring.security.oauth2.client.registration.google.client-id}")
  private String googleClientId;

  @GetMapping("/link-google")
  public RedirectView linkGoogleAccount(@AuthenticationPrincipal User currentUser,
      HttpSession session) {
    log.info("User {} is initiating Google account linking.", currentUser.getEmail());
    session.setAttribute("linkingUserId", currentUser.getId());
    return new RedirectView("/oauth2/authorization/google");
  }

  @PostMapping("/init-google-link")
  public ResponseEntity<MessageResponseDto> initGoogleLink(
      @AuthenticationPrincipal User currentUser) {
    String tempToken = UUID.randomUUID().toString();
    redisTemplate.opsForValue()
        .set("google-link:" + tempToken, currentUser.getId().toString(), Duration.ofMinutes(1));
    return ResponseEntity.ok(new MessageResponseDto("/profile/link-google?linkToken=" + tempToken));
  }
}