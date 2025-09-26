package ifortex.shuman.uladzislau.authservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponseDto {

  private final Instant timestamp;
  private String error;
  private final String message;
  private final String path;
  private List<String> details;
  private String errorCode;

  public ErrorResponseDto(HttpStatus httpStatus, String message, String path) {
    this.timestamp = Instant.now();
    this.error = httpStatus.getReasonPhrase();
    this.message = message;
    this.path = path;
  }

  public ErrorResponseDto(HttpStatus httpStatus, String message, String path,
      List<String> details) {
    this(httpStatus, message, path);
    this.details = details;
  }

  public ErrorResponseDto(String message, String path, List<String> details) {
    this(message, path);
    this.details = details;
  }

  public ErrorResponseDto(String message, String path) {
    this.timestamp = Instant.now();
    this.message = message;
    this.path = path;
  }

  public ErrorResponseDto(String message, String path, String errorCode) {
    this(message, path);
    this.errorCode = errorCode;
  }
}