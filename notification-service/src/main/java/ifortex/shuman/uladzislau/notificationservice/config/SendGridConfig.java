package ifortex.shuman.uladzislau.notificationservice.config;

import com.sendgrid.SendGrid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SendGridConfig {

    @Value("${spring.sendgrid.api-key}")
    private String sendgridApiKey;

    @Bean
    public SendGrid sendGrid() {
        if (sendgridApiKey == null || sendgridApiKey.isBlank()) {
            throw new IllegalStateException("SendGrid API key is not configured. Please set SENDGRID_API_KEY environment variable.");
        }
        return new SendGrid(sendgridApiKey);
    }
}