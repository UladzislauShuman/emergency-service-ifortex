package ifortex.shuman.uladzislau.notificationservice.service.implemention;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import ifortex.shuman.uladzislau.notificationservice.config.properties.EmailProperties;
import ifortex.shuman.uladzislau.notificationservice.dto.EmailNotificationDto;
import ifortex.shuman.uladzislau.notificationservice.service.EmailService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

  private final SendGrid sendGrid;
  private final EmailProperties emailProperties;

  @Override
  public void sendEmail(EmailNotificationDto dto) {
    String subject = emailProperties.getSubjects().get(dto.getSubjectKey());
    String bodyTemplate = emailProperties.getBodies().get(dto.getBodyTemplateKey());

    if (subject == null || bodyTemplate == null) {
      log.error("Missing email template for subject='{}', body='{}'",
          dto.getSubjectKey(), dto.getBodyTemplateKey());
      return;
    }

    String body = buildBody(bodyTemplate, dto);
    Mail mail = buildMail(dto, subject, body);
    send(mail, dto.getTo(), subject);
  }

  private String buildBody(String bodyTemplate, EmailNotificationDto dto) {
    Map<String, Object> model = dto.getTemplateModel();
    if (model == null || model.isEmpty()) {
      return bodyTemplate;
    }

    return switch (dto.getBodyTemplateKey()) {
      case "kyc-approval" -> String.format(bodyTemplate, model.get("userName"));
      case "kyc-rejection" ->
          String.format(bodyTemplate, model.get("userName"), model.get("reason"));
      case "email-change-alert" ->
          String.format(bodyTemplate, model.get("oldEmail"), model.get("newEmail"));
      case "paramedic-kyc-approval-with-credentials" -> String.format(
          bodyTemplate,
          model.get("userName"),
          model.get("workEmail"),
          model.get("workEmailPassword"),
          model.get("workEmail"));
      default -> {
        Object value = model.values().stream().findFirst().orElse("");
        yield String.format(bodyTemplate, value);
      }
    };
  }

  private Mail buildMail(EmailNotificationDto dto, String subject, String body) {
    Email from = new Email(emailProperties.getFromAddress(), emailProperties.getFromName());
    Email to = new Email(dto.getTo());
    Content content = new Content("text/plain", body);
    return new Mail(from, subject, to, content);
  }

  private void send(Mail mail, String recipient, String subject) {
    Request request = new Request();
    try {
      request.setMethod(Method.POST);
      request.setEndpoint("mail/send");
      request.setBody(mail.build());

      log.info("Sending email. To: [{}], Subject: [{}]", recipient, subject);
      Response response = sendGrid.api(request);

      if (response.getStatusCode() < 200 || response.getStatusCode() >= 300) {
        throw new IOException("SendGrid API returned status " + response.getStatusCode() +
            ", body: " + response.getBody());
      }

      log.info("Email sent successfully to {}. Status: {}", recipient, response.getStatusCode());

    } catch (IOException ex) {
      log.error("Failed to send email to {}", recipient, ex);
      throw new RuntimeException("Failed to send email to " + recipient, ex);
    }
  }
}
