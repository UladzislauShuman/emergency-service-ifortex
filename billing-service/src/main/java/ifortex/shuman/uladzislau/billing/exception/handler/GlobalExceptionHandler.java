package ifortex.shuman.uladzislau.billing.exception.handler;

import ifortex.shuman.uladzislau.billing.dto.ErrorResponseDto;
import ifortex.shuman.uladzislau.billing.exception.BillingException;
import ifortex.shuman.uladzislau.billing.exception.EntityNotFoundException;
import ifortex.shuman.uladzislau.billing.exception.OperationForbiddenException;
import ifortex.shuman.uladzislau.billing.exception.ResourceConflictException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "ifortex.shuman.uladzislau.billing")
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(BillingException.class)
  public ResponseEntity<ErrorResponseDto> handleBillingException(BillingException ex,
      HttpServletRequest request) {
    logException("Billing service error", request, ex);
    ErrorResponseDto errorResponse = new ErrorResponseDto(
        HttpStatus.SERVICE_UNAVAILABLE,
        "The billing service is temporarily unavailable. Please try again later.",
        request.getRequestURI()
    );
    return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
  }


  // 404
  @ExceptionHandler({
      EntityNotFoundException.class})
  public ResponseEntity<ErrorResponseDto> handleUserNotFoundException(
      RuntimeException ex, HttpServletRequest request) {
    logException("UserNotFoundException", request, ex);
    ErrorResponseDto errorResponse =
        new ErrorResponseDto(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  // 409
  @ExceptionHandler({ResourceConflictException.class})
  public ResponseEntity<ErrorResponseDto> handleConflictExceptions(
      RuntimeException ex, HttpServletRequest request) {
    logException("Conflict", request, ex);
    ErrorResponseDto errorResponse =
        new ErrorResponseDto(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
    return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
  }

  // 403
  @ExceptionHandler({OperationForbiddenException.class})
  public ResponseEntity<ErrorResponseDto> handleAccessDeniedException(
      RuntimeException ex, HttpServletRequest request) {
    logException(
        "Access Denied: User attempted to access a protected resource without required permissions",
        request, ex);
    ErrorResponseDto errorResponse = new ErrorResponseDto(HttpStatus.FORBIDDEN, "Access is denied",
        request.getRequestURI());
    return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
  }

  private void logException(String logMessage, HttpServletRequest request, Exception exception) {
    if (log.isDebugEnabled()) {
      log.debug("{} at path [{}]:", logMessage, request.getRequestURI(), exception);
    } else {
      log.warn("{} at path [{}]: {}", logMessage, request.getRequestURI(), exception.getMessage());
    }
  }
}