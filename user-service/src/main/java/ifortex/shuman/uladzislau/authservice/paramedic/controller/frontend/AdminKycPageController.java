package ifortex.shuman.uladzislau.authservice.paramedic.controller.frontend;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AdminKycPageController {
  @GetMapping("/admin/kyc-dashboard")
  public String kycDashboardPage() {
    return "admin-kyc-dashboard";
  }

  @GetMapping("/admin/kyc-details")
  public String kycDetailsPage() {
    return "admin-kyc-details";
  }
}
