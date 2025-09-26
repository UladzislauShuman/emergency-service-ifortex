package ifortex.shuman.uladzislau.gateway.config;

import static java.util.Map.entry;

import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Component
public class RouterValidator {

  private final AntPathMatcher pathMatcher = new AntPathMatcher();

  public static final List<String> openApiEndpoints = List.of(
      "/api/auth/registration",
      "/api/auth/login",
      "/api/auth/refresh",
      "/api/public/**", // kyc
      "/stripe-webhook",
      "/api/auth/verification/email",
      "/api/auth/verification/2fa",
      "/api/auth/password-request-reset",
      "/api/auth/password-admin-link",
      "/api/auth/otp/resend"
  );

  public static final List<String> authenticatedEndpoints = List.of( // only valid jwt
      "/api/profile/**",
      "/api/home/**",
      "/api/auth/logout",
      "/profile/**",
      "/api/users/{userId}/deletion-request"
  );

  public static final Map<String, String> securedEndpoints = Map.ofEntries(
      entry("POST /api/users/search", "admin:user:read"),
      entry("GET /api/users/{userId}", "admin:user:read"),
      entry("PATCH /api/users/{userId}", "admin:user:update"),
      entry("PUT /api/users/{userId}/lock", "admin:user:block"),
      entry("DELETE /api/users/{userId}/soft", "admin:user:delete:soft"),
      entry("DELETE /api/users/{userId}/hard", "admin:user:delete:hard"),
      entry("DELETE /api/users/{userId}/deletion-request", "admin:user:update"),
      entry("POST /api/users/actions/{userId}/send-reset-link", "admin:user:reset-password:reset-link"),
      entry("POST /api/users/actions/{userId}/generate-temp-password", "admin:user:reset-password:generate-temp"),
      entry("POST /api/users/client", "admin:create:client"),
      entry("POST /api/users/paramedic", "admin:create:paramedic"),
      entry("POST /api/users/admin", "admin:create:admin"),
      entry("POST /api/users/super-admin", "admin:create:super_admin"),
      entry("GET /api/admin/kyc/applications", "admin:kyc:read"),
      entry("GET /api/admin/kyc/applications/{applicationId}", "admin:kyc:read"),
      entry("PATCH /api/admin/kyc/applications/{applicationId}", "admin:kyc:manage"),
      entry("GET /api/admin/kyc/applications/{applicationId}/documents/identity", "admin:kyc:read"),
      entry("GET /api/admin/kyc/applications/{applicationId}/documents/selfie", "admin:kyc:read"),
      entry("GET /api/admin/kyc/applications/{applicationId}/documents/certificate", "admin:kyc:read"),
      entry("POST /api/subscription/checkout-sessions", "subscription:create"),
      entry("POST /api/subscription/portal-sessions", "subscription:manage"),
      entry("GET /api/subscription/details", "subscription:manage")
  );

  public Predicate<ServerHttpRequest> isSecured =
      request -> openApiEndpoints
          .stream()
          .noneMatch(uri -> pathMatcher.match(uri, request.getURI().getPath()));

  public String getRequiredPermission(ServerHttpRequest request) {
    String requestPath = request.getURI().getPath();
    HttpMethod requestMethod = request.getMethod();

    for (Map.Entry<String, String> entry : securedEndpoints.entrySet()) {
      String[] parts = entry.getKey().split(" ");
      HttpMethod method = HttpMethod.valueOf(parts[0]);
      String path = parts[1];

      if (requestMethod.equals(method) && pathMatcher.match(path, requestPath)) {
        return entry.getValue();
      }
    }

    for (String path : authenticatedEndpoints) {
      if (pathMatcher.match(path, requestPath)) {
        return null;
      }
    }

    throw new SecurityException(
        "No security configuration found for this endpoint: " + requestPath);
  }
}