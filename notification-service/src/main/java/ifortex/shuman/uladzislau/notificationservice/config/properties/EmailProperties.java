package ifortex.shuman.uladzislau.notificationservice.config.properties;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "application.email")
@Getter
@Setter
public class EmailProperties {
    private String fromAddress;
    private String fromName;
    private Map<String, String> subjects;
    private Map<String, String> bodies;
}