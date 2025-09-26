package ifortex.shuman.uladzislau.billing.config;

import com.stripe.Stripe;
import ifortex.shuman.uladzislau.billing.config.properties.StripeProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class StripeConfiguration {

  private final StripeProperties stripeProperties;

  @PostConstruct
  public void initStripe() {
    String secretKey = stripeProperties.getApiSecretKey();
    if (secretKey == null || secretKey.isBlank()) {
      log.warn("Stripe secret key is not configured. Stripe integration will not work.");
      return;
    }
    Stripe.apiKey = secretKey;
    log.info("Stripe API Key initialized successfully.");
  }
}