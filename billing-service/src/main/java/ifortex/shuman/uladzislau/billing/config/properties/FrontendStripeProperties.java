package ifortex.shuman.uladzislau.billing.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "application.frontend")
@Getter
@Setter
public class FrontendStripeProperties extends FrontendProperties {
    private String subscriptionSuccessUrl;
    private String subscriptionCancelUrl;
}