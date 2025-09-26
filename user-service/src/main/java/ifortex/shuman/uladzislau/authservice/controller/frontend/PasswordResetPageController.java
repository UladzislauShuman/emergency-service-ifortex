package ifortex.shuman.uladzislau.authservice.controller.frontend;

import ifortex.shuman.uladzislau.authservice.dto.AdminPasswordResetConfirmDto;
import ifortex.shuman.uladzislau.authservice.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class PasswordResetPageController {

  private final AuthenticationService authenticationService;

  @GetMapping("/password/reset/form")
  public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
    model.addAttribute("token", token);
    AdminPasswordResetConfirmDto dto = new AdminPasswordResetConfirmDto();
    dto.setToken(token);
    model.addAttribute("dto", dto);
    return "reset-password-form";
  }
}