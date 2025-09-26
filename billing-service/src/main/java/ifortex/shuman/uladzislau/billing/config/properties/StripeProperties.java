package ifortex.shuman.uladzislau.billing.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "stripe")
@Data
public class StripeProperties {
  private String apiSecretKey;
  private String webhookSecret;
}