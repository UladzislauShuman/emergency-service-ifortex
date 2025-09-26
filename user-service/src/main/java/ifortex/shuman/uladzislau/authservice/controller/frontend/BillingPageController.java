package ifortex.shuman.uladzislau.authservice.controller.frontend;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BillingPageController {

  @GetMapping("/account")
  public String showAccountPage() {
    return "account";
  }

  @GetMapping("/payment/success")
  public String paymentSuccess() {
    return "payment-success";
  }

  @GetMapping("/payment/cancel")
  public String paymentCancel() {
    return "payment-cancel";
  }
}