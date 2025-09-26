package ifortex.shuman.uladzislau.billing.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "application.frontend")
@Getter
@Setter
public class FrontendProperties {
    private String baseUrl;
    private String adminResetPasswordPath;
}