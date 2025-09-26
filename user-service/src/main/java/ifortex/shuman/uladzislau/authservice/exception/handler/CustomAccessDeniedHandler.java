package ifortex.shuman.uladzislau.authservice.exception.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import ifortex.shuman.uladzislau.authservice.dto.ErrorResponseDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {

        log.warn("Access Denied for user '{}' attempting to access '{}'. Reason: {}",
                request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "Anonymous",
                request.getRequestURI(),
                accessDeniedException.getMessage());

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponseDto errorResponse = new ErrorResponseDto(
                HttpStatus.FORBIDDEN,
                "Access Denied: You do not have the required permissions to access this resource.",
                request.getRequestURI()
        );

        response.getOutputStream().write(objectMapper.writeValueAsBytes(errorResponse));
    }
}