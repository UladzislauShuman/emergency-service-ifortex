package ifortex.shuman.uladzislau.authservice.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "application.security.otp")
@Getter
@Setter
public class OtpProperties {

  private int length = 6;
  private int resendCooldownSeconds = 60;
  private Map<String, Duration> expiration;
}