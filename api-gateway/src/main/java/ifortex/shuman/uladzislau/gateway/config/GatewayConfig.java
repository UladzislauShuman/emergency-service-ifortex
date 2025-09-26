package ifortex.shuman.uladzislau.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

  private final AuthenticationFilter filter;

  public GatewayConfig(AuthenticationFilter filter) {
    this.filter = filter;
  }

  @Bean
  public RouteLocator routes(RouteLocatorBuilder builder) {
    return builder.routes()
        .route("internal-api-user-service", r -> r.path("/api/internal/users/**")
            .uri("lb://user-service"))

        .route("internal-api-billing-service", r -> r.path("/api/internal/subscriptions/**")
            .uri("lb://billing-service"))

        .route("public-api-kyc", r -> r.path("/api/public/**")
            .uri("lb://user-service"))

        .route("stripe-webhook-public", r -> r.path("/stripe-webhook")
            .uri("lb://billing-service"))

        .route("billing-service-api", r -> r.path("/api/subscription/**")
            .filters(f -> f.filter(filter))
            .uri("lb://billing-service"))

        .route("user-service-api", r -> r.path("/api/**")
            .filters(f -> f.filter(filter))
            .uri("lb://user-service"))

        .route("oauth2-routes", r -> r.path("/oauth2/**", "/login/oauth2/**")
            .uri("lb://user-service"))

        .route("user-service-profile-pages", r -> r.path("/profile/**")
            .filters(f -> f.filter(filter))
            .uri("lb://user-service"))

        .route("frontend-catch-all", r -> r.path("/**")
            .uri("lb://user-service"))

        .build();
  }
}