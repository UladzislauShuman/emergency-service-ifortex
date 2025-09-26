package ifortex.shuman.uladzislau.notificationservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

  public static final String EMAIL_NOTIFICATION_QUEUE = "q.email-notification";

  @Bean
  public Queue emailNotificationQueue() {
    return new Queue(EMAIL_NOTIFICATION_QUEUE, true);
  }

  @Bean
  public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
    return new Jackson2JsonMessageConverter(objectMapper);
  }
}