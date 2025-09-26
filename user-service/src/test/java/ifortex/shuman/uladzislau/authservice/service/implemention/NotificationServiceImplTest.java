package ifortex.shuman.uladzislau.authservice.service.implemention;

import ifortex.shuman.uladzislau.authservice.config.NotificationRabbitMQConfig;
import ifortex.shuman.uladzislau.authservice.dto.EmailNotificationDto;
import ifortex.shuman.uladzislau.authservice.model.OtpType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

  @Mock
  private RabbitTemplate rabbitTemplate;

  @InjectMocks
  private NotificationServiceImpl notificationService;

  @Test
  void sendOtp_shouldConstructCorrectDtoAndSendToQueue() {
    String testEmail = "test@example.com";
    String testOtp = "123456";
    OtpType mockOtpType = mock(OtpType.class);
    when(mockOtpType.getTemplateKey()).thenReturn("registration-template");

    notificationService.sendOtp(testEmail, testOtp, mockOtpType);

    ArgumentCaptor<EmailNotificationDto> dtoCaptor = ArgumentCaptor.forClass(
        EmailNotificationDto.class);
    verify(rabbitTemplate).convertAndSend(
        eq(NotificationRabbitMQConfig.EMAIL_NOTIFICATION_QUEUE),
        dtoCaptor.capture()
    );

    EmailNotificationDto capturedDto = dtoCaptor.getValue();
    assertEquals(testEmail, capturedDto.getTo());
    assertEquals("registration-template", capturedDto.getSubjectKey());
    assertEquals("registration-template", capturedDto.getBodyTemplateKey());
    assertEquals(testOtp, capturedDto.getTemplateModel().get("value"));
  }

  @Test
  void sendPasswordResetEmail_shouldConstructCorrectDtoAndSendToQueue() {
    String testEmail = "reset@example.com";
    String resetUrl = "http://example.com/reset?token=xyz";

    notificationService.sendPasswordResetEmail(testEmail, resetUrl);

    ArgumentCaptor<EmailNotificationDto> dtoCaptor = ArgumentCaptor.forClass(
        EmailNotificationDto.class);
    verify(rabbitTemplate).convertAndSend(
        eq(NotificationRabbitMQConfig.EMAIL_NOTIFICATION_QUEUE),
        dtoCaptor.capture()
    );

    EmailNotificationDto capturedDto = dtoCaptor.getValue();
    assertEquals(testEmail, capturedDto.getTo());
    assertEquals("password-reset-link", capturedDto.getSubjectKey());
    assertEquals("password-reset-link", capturedDto.getBodyTemplateKey());
    assertEquals(resetUrl, capturedDto.getTemplateModel().get("value"));
  }

  @Test
  void sendTemporaryPasswordEmail_shouldConstructCorrectDtoAndSendToQueue() {
    String testEmail = "temp@example.com";
    String tempPassword = "tempPassword123!";

    notificationService.sendTemporaryPasswordEmail(testEmail, tempPassword);

    ArgumentCaptor<EmailNotificationDto> dtoCaptor = ArgumentCaptor.forClass(
        EmailNotificationDto.class);
    verify(rabbitTemplate).convertAndSend(
        eq(NotificationRabbitMQConfig.EMAIL_NOTIFICATION_QUEUE),
        dtoCaptor.capture()
    );

    EmailNotificationDto capturedDto = dtoCaptor.getValue();
    assertEquals(testEmail, capturedDto.getTo());
    assertEquals("temporary-password", capturedDto.getSubjectKey());
    assertEquals("temporary-password", capturedDto.getBodyTemplateKey());
    assertEquals(tempPassword, capturedDto.getTemplateModel().get("value"));
  }

//  @Test
//  void sendEmailChangeNotificationToOldEmail_shouldConstructCorrectDtoAndSendToQueue() {
//    String oldEmail = "old@example.com";
//    String newEmail = "new@example.com";
//
//    notificationService.sendEmailChangeNotificationToOldEmail(oldEmail, newEmail);
//
//    ArgumentCaptor<EmailNotificationDto> dtoCaptor = ArgumentCaptor.forClass(EmailNotificationDto.class);
//    verify(rabbitTemplate).convertAndSend(
//        eq(NotificationRabbitMQConfig.EMAIL_NOTIFICATION_QUEUE),
//        dtoCaptor.capture()
//    );
//
//    EmailNotificationDto capturedDto = dtoCaptor.getValue();
//    assertEquals(oldEmail, capturedDto.getTo());
//    assertEquals("email-change-alert", capturedDto.getSubjectKey());
//    assertEquals("email-change-alert", capturedDto.getBodyTemplateKey());
//    assertTrue(capturedDto.getTemplateModel().isEmpty());
//  }
}