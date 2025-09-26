package ifortex.shuman.uladzislau.billing.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "stripe")
@Data
public class StripePlanMappingProperties {
    private Map<String, String> planMapping;
}