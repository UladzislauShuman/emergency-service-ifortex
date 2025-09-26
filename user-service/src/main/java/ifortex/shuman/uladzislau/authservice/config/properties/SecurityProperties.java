package ifortex.shuman.uladzislau.authservice.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "application.security")
@Getter
@Setter
public class SecurityProperties {
    private Duration passwordResetTokenExpiration = Duration.ofHours(1);
}