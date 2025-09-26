package ifortex.shuman.uladzislau.gateway.config;

import ifortex.shuman.uladzislau.gateway.util.JwtUtil;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class AuthenticationFilter implements GatewayFilter {

  private final RouterValidator routerValidator;
  private final JwtUtil jwtUtil;

  public AuthenticationFilter(RouterValidator routerValidator, JwtUtil jwtUtil) {
    this.routerValidator = routerValidator;
    this.jwtUtil = jwtUtil;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    ServerHttpRequest request = exchange.getRequest();

    if (!routerValidator.isSecured.test(request)) {
      return chain.filter(exchange);
    }

    log.debug("Request to secured endpoint: {}", request.getURI());

    String token = extractTokenFromRequest(request);
    if (token == null || token.isEmpty()) {
      return onError(exchange, "Authorization token is missing in request", HttpStatus.UNAUTHORIZED);
    }

    try {
      if (jwtUtil.isTokenExpired(token)) {
        return onError(exchange, "Authorization token is expired", HttpStatus.UNAUTHORIZED);
      }

      String requiredPermission = routerValidator.getRequiredPermission(request);
      if (requiredPermission != null) {
        List<String> userPermissions = jwtUtil.getPermissions(token);
        boolean hasPermission;

        if (request.getURI().getPath().endsWith("/api/subscription/details")) {
          hasPermission = userPermissions.contains("subscription:create") || userPermissions.contains("subscription:manage");
        } else {
          hasPermission = userPermissions.contains(requiredPermission);
        }

        if (!hasPermission) {
          log.warn("User does not have required permission ('{}') for endpoint {}", requiredPermission, request.getURI());
          return this.onError(exchange, "Access Denied: Insufficient permissions", HttpStatus.FORBIDDEN);
        }
      }

      String userId = jwtUtil.getUserId(token);
      ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
          .header("X-User-Id", userId)
          .build();

      ServerWebExchange modifiedExchange = exchange.mutate().request(modifiedRequest).build();

      return chain.filter(modifiedExchange);

    } catch (SecurityException se) {
      log.error("Security configuration error: {}", se.getMessage());
      return this.onError(exchange, "Internal Server Error: Security policy not defined", HttpStatus.INTERNAL_SERVER_ERROR);
    } catch (Exception e) {
      log.error("Error processing token: {}", e.getMessage(), e);
      return this.onError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
    }
  }

  private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
    ServerHttpResponse response = exchange.getResponse();
    response.setStatusCode(httpStatus);
    log.warn("Auth Filter Error: '{}' for request: {}", err, exchange.getRequest().getURI());
    response.getHeaders().add("Content-Type", "application/json");
    String errorBody = "{\"status\":" + httpStatus.value() + ", \"error\":\"" + err + "\"}";
    return response.writeWith(Mono.just(response.bufferFactory().wrap(errorBody.getBytes())));
  }

  private String extractTokenFromRequest(ServerHttpRequest request) {
    List<String> authHeaders = request.getHeaders().getOrEmpty("Authorization");
    if (!authHeaders.isEmpty()) {
      String authHeader = authHeaders.get(0);
      if (authHeader != null && authHeader.startsWith("Bearer ")) {
        return authHeader.substring(7);
      }
    }

    if (request.getURI().getPath().equals("/profile/link-google")) {
      return request.getQueryParams().getFirst("token");
    }

    return null;
  }
}