package ifortex.shuman.uladzislau.authservice.controller.frontend;

import ifortex.shuman.uladzislau.authservice.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AuthPageController {

  @GetMapping("/home")
  public String homePage() {
    return "home";
  }

  @GetMapping("/login")
  public String loginPage() {
    return "login";
  }

  @GetMapping("/register")
  public String registerPage() {
    return "register";
  }

  @GetMapping("/client-register")
  public String clientRegisterPage() {
    return "client-register";
  }

  @GetMapping("/verify-email")
  public String verifyEmailPage(@RequestParam String email, Model model) {
    model.addAttribute("email", email);
    return "verify-email";
  }

  @GetMapping("/verify-2fa")
  public String verify2faPage(@RequestParam String email, Model model) {
    model.addAttribute("email", email);
    return "verify-2fa";
  }

  @GetMapping("/")
  public String rootPage() {
    return "index";
  }

  @GetMapping("/paramedic/kyc-form")
  public String kycFormPage() {
    return "paramedic-kyc-form";
  }

  @GetMapping("/login/oauth2/success")
  public String oauth2SuccessPage() {
    return "oauth2-success";
  }

  @GetMapping("/paramedic/verify-email")
  public String paramedicVerifyEmailPage(@RequestParam String email, Model model) {
    model.addAttribute("email", email);
    return "paramedic-verify-email";
  }

  @GetMapping("/paramedic/application-status")
  public String paramedicApplicationStatusPage(@RequestParam String email, Model model) {
    model.addAttribute("email", email);
    return "paramedic-application-status";
  }

  @GetMapping("/force-change-password")
  public String forceChangePasswordPage() {
    return "force-change-password";
  }
}