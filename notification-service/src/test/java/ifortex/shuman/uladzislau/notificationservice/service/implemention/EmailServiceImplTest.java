package ifortex.shuman.uladzislau.notificationservice.service.implemention;

import com.jayway.jsonpath.JsonPath;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import ifortex.shuman.uladzislau.notificationservice.config.properties.EmailProperties;
import ifortex.shuman.uladzislau.notificationservice.dto.EmailNotificationDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.never;

import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

  @Mock
  private SendGrid sendGrid;

  @Mock
  private EmailProperties emailProperties;

  @InjectMocks
  private EmailServiceImpl emailService;

  @Test
  void sendEmail_whenValidDtoAndTemplatesExist_shouldSendEmailSuccessfully() throws IOException {
    EmailNotificationDto dto = new EmailNotificationDto(
        "test@example.com",
        "test.subject",
        "test.body",
        null
    );

    when(emailProperties.getSubjects()).thenReturn(Map.of("test.subject", "Test Subject"));
    when(emailProperties.getBodies()).thenReturn(Map.of("test.body", "This is a test body."));
    when(emailProperties.getFromAddress()).thenReturn("from@example.com");
    when(emailProperties.getFromName()).thenReturn("Test Sender");

    Response response = new Response();
    response.setStatusCode(202);
    when(sendGrid.api(any(Request.class))).thenReturn(response);

    emailService.sendEmail(dto);

    ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
    verify(sendGrid).api(requestCaptor.capture());

    Request capturedRequest = requestCaptor.getValue();
    String requestBody = capturedRequest.getBody();

    assertEquals(Method.POST, capturedRequest.getMethod());
    assertEquals("mail/send", capturedRequest.getEndpoint());

    assertEquals("Test Subject", JsonPath.read(requestBody, "$.subject"));
    assertEquals("from@example.com", JsonPath.read(requestBody, "$.from.email"));
    assertEquals("Test Sender", JsonPath.read(requestBody, "$.from.name"));
    assertEquals("test@example.com", JsonPath.read(requestBody, "$.personalizations[0].to[0].email"));
    assertEquals("This is a test body.", JsonPath.read(requestBody, "$.content[0].value"));
    assertEquals("text/plain", JsonPath.read(requestBody, "$.content[0].type"));
  }

  @Test
  void sendEmail_whenDtoContainsTemplateModel_shouldFormatBodyCorrectly() throws IOException {
    EmailNotificationDto dto = new EmailNotificationDto(
        "user@example.com",
        "otp.subject",
        "otp.body",
        Map.of("otp", "123456")
    );

    when(emailProperties.getSubjects()).thenReturn(Map.of("otp.subject", "Your Verification Code"));
    when(emailProperties.getBodies()).thenReturn(Map.of("otp.body", "Your OTP code is: %s. It is valid for 5 minutes."));
    when(emailProperties.getFromAddress()).thenReturn("no-reply@service.com");
    when(emailProperties.getFromName()).thenReturn("My Service");

    Response response = new Response();
    response.setStatusCode(202);
    when(sendGrid.api(any(Request.class))).thenReturn(response);

    emailService.sendEmail(dto);

    ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
    verify(sendGrid).api(requestCaptor.capture());

    Request capturedRequest = requestCaptor.getValue();
    String requestBody = capturedRequest.getBody();

    String expectedBody = "Your OTP code is: 123456. It is valid for 5 minutes.";
    assertEquals(expectedBody, JsonPath.read(requestBody, "$.content[0].value"));
    assertEquals("user@example.com", JsonPath.read(requestBody, "$.personalizations[0].to[0].email"));
    assertEquals("Your Verification Code", JsonPath.read(requestBody, "$.subject"));
  }

  @Test
  void sendEmail_whenSubjectTemplateIsMissing_shouldLogErrorAndNotSendEmail() throws IOException {
    EmailNotificationDto dto = new EmailNotificationDto(
        "test@example.com",
        "nonexistent.subject",
        "test.body",
        null
    );

    when(emailProperties.getSubjects()).thenReturn(Map.of("some.other.subject", "Some Subject"));
    when(emailProperties.getBodies()).thenReturn(Map.of("test.body", "This is a test body."));

    emailService.sendEmail(dto);

    verify(sendGrid, never()).api(any(Request.class));
  }

  @Test
  void sendEmail_whenBodyTemplateIsMissing_shouldLogErrorAndNotSendEmail() throws IOException {
    EmailNotificationDto dto = new EmailNotificationDto(
        "test@example.com",
        "test.subject",
        "nonexistent.body",
        null
    );

    when(emailProperties.getSubjects()).thenReturn(Map.of("test.subject", "Test Subject"));
    when(emailProperties.getBodies()).thenReturn(Map.of("some.other.body", "Some Body"));

    emailService.sendEmail(dto);

    verify(sendGrid, never()).api(any(Request.class));
  }

  @Test
  void sendEmail_whenSendGridApiThrowsIOException_shouldThrowRuntimeException() throws IOException {
    EmailNotificationDto dto = new EmailNotificationDto(
        "test@example.com",
        "test.subject",
        "test.body",
        null
    );

    when(emailProperties.getSubjects()).thenReturn(Map.of("test.subject", "Test Subject"));
    when(emailProperties.getBodies()).thenReturn(Map.of("test.body", "This is a test body."));
    when(emailProperties.getFromAddress()).thenReturn("from@example.com");

    when(sendGrid.api(any(Request.class))).thenThrow(new IOException("API call failed"));

    assertThrows(RuntimeException.class, () -> emailService.sendEmail(dto));
  }

  @Test
  void sendEmail_whenSendGridReturnsErrorStatusCode_shouldThrowRuntimeException() throws IOException {
    EmailNotificationDto dto = new EmailNotificationDto(
        "test@example.com",
        "test.subject",
        "test.body",
        null
    );

    when(emailProperties.getSubjects()).thenReturn(Map.of("test.subject", "Test Subject"));
    when(emailProperties.getBodies()).thenReturn(Map.of("test.body", "This is a test body."));
    when(emailProperties.getFromAddress()).thenReturn("from@example.com");

    Response errorResponse = new Response();
    errorResponse.setStatusCode(400);
    errorResponse.setBody("{\"errors\":[{\"message\":\"Invalid API key\"}]}");
    when(sendGrid.api(any(Request.class))).thenReturn(errorResponse);

    assertThrows(RuntimeException.class, () -> emailService.sendEmail(dto));
  }
}