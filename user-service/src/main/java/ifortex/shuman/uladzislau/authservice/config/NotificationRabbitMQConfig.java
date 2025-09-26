package ifortex.shuman.uladzislau.authservice.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationRabbitMQConfig {

    public static final String EMAIL_NOTIFICATION_QUEUE = "q.email-notification";

    @Bean
    public Queue emailNotificationQueue() {
        return new Queue(EMAIL_NOTIFICATION_QUEUE, true);
    }
}