package ifortex.shuman.uladzislau.notificationservice.listener;

import ifortex.shuman.uladzislau.notificationservice.dto.EmailNotificationDto;
import ifortex.shuman.uladzislau.notificationservice.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailNotificationListenerTest {

  @Mock
  private EmailService emailService;

  @InjectMocks
  private EmailNotificationListener emailNotificationListener;

  @Test
  void handleEmailNotification_whenServiceSucceeds_shouldCallEmailService() {
    EmailNotificationDto dto = new EmailNotificationDto(
        "test@example.com",
        "test.subject",
        "test.body",
        null
    );

    emailNotificationListener.handleEmailNotification(dto);

    verify(emailService).sendEmail(dto);
  }

  @Test
  void handleEmailNotification_whenServiceThrowsException_shouldCatchExceptionAndNotRethrow() {
    EmailNotificationDto dto = new EmailNotificationDto(
        "fail@example.com",
        "fail.subject",
        "fail.body",
        null
    );
    RuntimeException testException = new RuntimeException("Email service is down");
    doThrow(testException).when(emailService).sendEmail(dto);

    assertDoesNotThrow(() -> emailNotificationListener.handleEmailNotification(dto));

    verify(emailService).sendEmail(dto);
  }
}