package ifortex.shuman.uladzislau.notificationservice.listener;

import ifortex.shuman.uladzislau.notificationservice.config.RabbitMQConfig;
import ifortex.shuman.uladzislau.notificationservice.dto.EmailNotificationDto;
import ifortex.shuman.uladzislau.notificationservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationListener {

  private final EmailService emailService;

  @RabbitListener(queues = RabbitMQConfig.EMAIL_NOTIFICATION_QUEUE)
  public void handleEmailNotification(EmailNotificationDto notificationDto) {
    log.info("Received email notification request: {}", notificationDto);
    try {
      emailService.sendEmail(notificationDto);
    } catch (Exception e) {
      log.error("Failed to process email notification request: {}. Error: {}", notificationDto,
          e.getMessage());
    }
  }
}