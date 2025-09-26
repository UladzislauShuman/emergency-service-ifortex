package ifortex.shuman.uladzislau.authservice.paramedic.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "azure.storage")
@Getter
@Setter
public class AzureStorageProperties {
    private String connectionString;
    private String containerName;
}
