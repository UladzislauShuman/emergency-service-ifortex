package ifortex.shuman.uladzislau.authservice.service.implemention;

import ifortex.shuman.uladzislau.authservice.config.NotificationRabbitMQConfig;
import ifortex.shuman.uladzislau.authservice.dto.EmailNotificationDto;
import ifortex.shuman.uladzislau.authservice.model.OtpType;
import ifortex.shuman.uladzislau.authservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

  private final RabbitTemplate rabbitTemplate;

  @Override
  public void sendOtp(String toEmail, String otp, OtpType otpType) {
    log.info("Sending OTP notification request for email {} and type {}", toEmail, otpType);
    EmailNotificationDto dto = new EmailNotificationDto(
        toEmail,
        otpType.getTemplateKey(),
        otpType.getTemplateKey(),
        Map.of("value", otp)
    );
    rabbitTemplate.convertAndSend(NotificationRabbitMQConfig.EMAIL_NOTIFICATION_QUEUE, dto);
  }

  @Override
  public void sendPasswordResetEmail(String toEmail, String resetUrl) {
    log.info("Sending password reset link notification request for email {}", toEmail);
    EmailNotificationDto dto = new EmailNotificationDto(
        toEmail,
        "password-reset-link",
        "password-reset-link",
        Map.of("value", resetUrl)
    );
    rabbitTemplate.convertAndSend(NotificationRabbitMQConfig.EMAIL_NOTIFICATION_QUEUE, dto);
  }

  @Override
  public void sendTemporaryPasswordEmail(String toEmail, String temporaryPassword) {
    log.info("Sending temporary password notification request for email {}", toEmail);
    EmailNotificationDto dto = new EmailNotificationDto(
        toEmail,
        "temporary-password",
        "temporary-password",
        Map.of("value", temporaryPassword)
    );
    rabbitTemplate.convertAndSend(NotificationRabbitMQConfig.EMAIL_NOTIFICATION_QUEUE, dto);
  }

  @Override
  public void sendEmailChangeNotificationToOldEmail(String oldEmail, String newEmail) {
    log.info("Sending email change security alert to old email {}", oldEmail);
    EmailNotificationDto dto = new EmailNotificationDto(
        oldEmail,
        "email-change-alert",
        "email-change-alert",
        Map.of("oldEmail", oldEmail, "newEmail", newEmail)
    );
    rabbitTemplate.convertAndSend(NotificationRabbitMQConfig.EMAIL_NOTIFICATION_QUEUE, dto);
  }

  @Override
  public void sendKycApprovalEmail(String toEmail, String userName) {
    log.info("Sending KYC approval notification request for email {}", toEmail);
    EmailNotificationDto dto = new EmailNotificationDto(
        toEmail,
        "kyc-approval",
        "kyc-approval",
        Map.of("userName", userName)
    );
    rabbitTemplate.convertAndSend(NotificationRabbitMQConfig.EMAIL_NOTIFICATION_QUEUE, dto);
  }

  @Override
  public void sendKycRejectionEmail(String toEmail, String userName, String reason) {
    log.info("Sending KYC rejection notification request for email {}", toEmail);
    EmailNotificationDto dto = new EmailNotificationDto(
        toEmail,
        "kyc-rejection",
        "kyc-rejection",
        Map.of("userName", userName, "reason", reason)
    );
    rabbitTemplate.convertAndSend(NotificationRabbitMQConfig.EMAIL_NOTIFICATION_QUEUE, dto);
  }

  @Override
  public void sendParamedicApprovalNotification(String applicantEmail, String userName,
      String workEmail, String workEmailPassword) {
    log.info("Sending Paramedic KYC approval notification with new credentials to {}",
        applicantEmail);
    EmailNotificationDto dto = new EmailNotificationDto(
        applicantEmail,
        "paramedic-kyc-approval-with-credentials",
        "paramedic-kyc-approval-with-credentials",
        Map.of(
            "userName", userName,
            "workEmail", workEmail,
            "workEmailPassword", workEmailPassword
        )
    );
    rabbitTemplate.convertAndSend(NotificationRabbitMQConfig.EMAIL_NOTIFICATION_QUEUE, dto);
  }
}