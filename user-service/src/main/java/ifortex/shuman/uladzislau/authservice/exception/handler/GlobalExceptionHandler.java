package ifortex.shuman.uladzislau.authservice.exception.handler;

import ifortex.shuman.uladzislau.authservice.dto.ErrorResponseDto;
import ifortex.shuman.uladzislau.authservice.exception.EmailAlreadyExistsException;
import ifortex.shuman.uladzislau.authservice.exception.EntityNotFoundException;
import ifortex.shuman.uladzislau.authservice.exception.InvalidOtpException;
import ifortex.shuman.uladzislau.authservice.exception.OperationForbiddenException;
import ifortex.shuman.uladzislau.authservice.exception.ResourceConflictException;
import ifortex.shuman.uladzislau.authservice.exception.TokenException;
import ifortex.shuman.uladzislau.authservice.exception.UserAccountLockedException;
import ifortex.shuman.uladzislau.authservice.exception.UserNotFoundException;
import ifortex.shuman.uladzislau.authservice.paramedic.exception.FileNotFoundInStorageException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice(basePackages = "ifortex.shuman.uladzislau.authservice")
@Slf4j
public class GlobalExceptionHandler {

  // 423
  @ExceptionHandler(LockedException.class)
  public ResponseEntity<ErrorResponseDto> handleLockedException(LockedException ex,
      HttpServletRequest request) {
    logException("Login failed: account is locked", request, ex);
    String message = "Your account is currently locked. Please contact support.";
    ErrorResponseDto errorResponse = new ErrorResponseDto(message, request.getRequestURI(),
        "ACCOUNT_LOCKED");
    return new ResponseEntity<>(errorResponse, HttpStatus.LOCKED);
  }

  // 404
  @ExceptionHandler({
      UserNotFoundException.class,
      EntityNotFoundException.class,
      FileNotFoundInStorageException.class})
  public ResponseEntity<ErrorResponseDto> handleUserNotFoundException(
      RuntimeException ex, HttpServletRequest request) {
    logException("UserNotFoundException", request, ex);
    ErrorResponseDto errorResponse =
        new ErrorResponseDto(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  // 409
  @ExceptionHandler({EmailAlreadyExistsException.class, ResourceConflictException.class,
      UserAccountLockedException.class})
  public ResponseEntity<ErrorResponseDto> handleConflictExceptions(
      RuntimeException ex, HttpServletRequest request) {
    logException("Conflict", request, ex);
    ErrorResponseDto errorResponse =
        new ErrorResponseDto(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
    return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
  }

  // 400
  @ExceptionHandler({IllegalArgumentException.class, InvalidOtpException.class})
  public ResponseEntity<ErrorResponseDto> handleBadRequestExceptions(
      RuntimeException ex, HttpServletRequest request) {
    logException("Bad Request Exception", request, ex);
    ErrorResponseDto errorResponse =
        new ErrorResponseDto(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  // 400
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponseDto> handleValidationExceptions(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    List<String> details = ex.getBindingResult().getFieldErrors().stream()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .collect(Collectors.toList());
    logException("Validation failed", request, ex);
    ErrorResponseDto errorResponse =
        new ErrorResponseDto(HttpStatus.BAD_REQUEST, "Validation Failed",
            request.getRequestURI(), details);
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  // 403
  @ExceptionHandler({AccessDeniedException.class, OperationForbiddenException.class})
  public ResponseEntity<ErrorResponseDto> handleAccessDeniedException(
      RuntimeException ex, HttpServletRequest request) {
    logException(
        "Access Denied: User attempted to access a protected resource without required permissions",
        request, ex);
    ErrorResponseDto errorResponse = new ErrorResponseDto(HttpStatus.FORBIDDEN, "Access is denied",
        request.getRequestURI());
    return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
  }

  // 401
  @ExceptionHandler(TokenException.class)
  public ResponseEntity<ErrorResponseDto> handleTokenException(
      TokenException ex, HttpServletRequest request) {
    logException("Token Exception", request, ex);
    ErrorResponseDto errorResponse =
        new ErrorResponseDto(HttpStatus.UNAUTHORIZED, ex.getMessage(), request.getRequestURI());
    return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
  }

  // 401
  @ExceptionHandler(CredentialsExpiredException.class)
  public ResponseEntity<ErrorResponseDto> handleCredentialsExpiredException(
      CredentialsExpiredException ex, HttpServletRequest request) {
    logException("Login failed", request, ex);
    String message = "The temporary password has expired. Please contact an administrator to reset it.";
    ErrorResponseDto errorResponse =
        new ErrorResponseDto(HttpStatus.UNAUTHORIZED, message, request.getRequestURI());

    return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
  }

  // 401
  @ExceptionHandler({
      BadCredentialsException.class, // wrong password
      DisabledException.class,        // isEnabled()=false
      InternalAuthenticationServiceException.class
  })
  public ResponseEntity<ErrorResponseDto> handleAuthenticationExceptions(
      Exception ex, HttpServletRequest request) {
    logException("Authentication failed", request, ex);
    String message = "Invalid credentials provided.";
    if (ex instanceof InternalAuthenticationServiceException
        && ex.getCause() instanceof UserNotFoundException) {
      log.debug("Login failed due to UserNotFoundException, returning generic error message.");
    }
    ErrorResponseDto errorResponse =
        new ErrorResponseDto(HttpStatus.UNAUTHORIZED, message, request.getRequestURI());

    return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
  }

  // 400
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponseDto> handleHttpMessageNotReadableException(
      HttpMessageNotReadableException ex, HttpServletRequest request) {

    String readableErrorMessage = "Invalid request body format.";

    Throwable cause = ex.getCause();
    if (cause instanceof com.fasterxml.jackson.databind.exc.InvalidFormatException ife
        && ife.getTargetType() != null && ife.getTargetType().isEnum()) {
      String validValues = Arrays.stream(ife.getTargetType().getEnumConstants())
          .map(Object::toString)
          .collect(Collectors.joining(", "));
      readableErrorMessage = String.format(
          "Invalid value '%s' for field '%s'. Allowed values are: [%s]",
          ife.getValue(),
          ife.getPath().get(ife.getPath().size() - 1).getFieldName(),
          validValues
      );
    }

    logException("Bad Request: " + readableErrorMessage, request, ex);
    ErrorResponseDto errorResponse = new ErrorResponseDto(HttpStatus.BAD_REQUEST,
        readableErrorMessage, request.getRequestURI());
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  // 500
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponseDto> handleAllOtherExceptions(Exception ex,
      HttpServletRequest request) {
    logException("An unexpected error occurred at path", request, ex);
    ErrorResponseDto errorResponse = new ErrorResponseDto(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "An internal server error occurred. Please try again later.", request.getRequestURI());
    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  // 404
  @ExceptionHandler(NoHandlerFoundException.class)
  public ResponseEntity<ErrorResponseDto> handleNoHandlerFoundException(NoHandlerFoundException ex,
      HttpServletRequest request) {
    log.warn("Handler not found for path [{}]: {}", request.getRequestURI(), ex.getMessage());
    ErrorResponseDto errorResponse = new ErrorResponseDto(HttpStatus.NOT_FOUND,
        "The requested resource was not found.", request.getRequestURI());
    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  // 400
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponseDto> handleMethodArgumentTypeMismatch(
      MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

    String error = String.format(
        "The parameter '%s' with value '%s' could not be converted to type '%s'",
        ex.getName(), ex.getValue(), ex.getRequiredType().getSimpleName());

    logException("Bad Request: " + error, request, ex);
    ErrorResponseDto errorResponse = new ErrorResponseDto(HttpStatus.BAD_REQUEST, error,
        request.getRequestURI());
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  private void logException(String logMessage, HttpServletRequest request, Exception exception) {
    if (log.isDebugEnabled()) {
      log.debug("{} at path [{}]:", logMessage, request.getRequestURI(), exception);
    } else {
      log.warn("{} at path [{}]: {}", logMessage, request.getRequestURI(), exception.getMessage());
    }
  }
}