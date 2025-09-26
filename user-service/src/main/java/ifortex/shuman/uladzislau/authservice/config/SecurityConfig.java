package ifortex.shuman.uladzislau.authservice.config;

import ifortex.shuman.uladzislau.authservice.exception.handler.CustomAccessDeniedHandler;
import ifortex.shuman.uladzislau.authservice.service.JwtService;
import ifortex.shuman.uladzislau.authservice.service.UserTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.ForwardedHeaderFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

  private final AuthenticationProvider authenticationProvider;
  private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
  private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
  private final CustomAccessDeniedHandler customAccessDeniedHandler;

  @Bean
  public ForwardedHeaderFilter forwardedHeaderFilter() {
    return new ForwardedHeaderFilter();
  }

  @Bean
  public JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService,
      UserDetailsService userDetailsService, UserTokenService userTokenService) {
    return new JwtAuthenticationFilter(jwtService, userDetailsService, userTokenService);
  }

  @Bean
  @Order(0)
  public SecurityFilterChain internalApiFilterChain(HttpSecurity http) throws Exception {
    http
        .securityMatcher("/api/internal/**")
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
    return http.build();
  }

  @Bean
  @Order(1)
  public SecurityFilterChain oauth2SecurityFilterChain(HttpSecurity http) throws Exception {
    http
        .securityMatcher("/oauth2/**", "/login/oauth2/**")
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .csrf(AbstractHttpConfigurer::disable)
        .oauth2Login(oauth2 ->
            oauth2.successHandler(oAuth2AuthenticationSuccessHandler)
        );
    return http.build();
  }

  @Bean
  @Order(2)
  public SecurityFilterChain publicAuthApiFilterChain(HttpSecurity http) throws Exception {
    http
        .securityMatcher(
            "/api/auth/**",
            "/stripe-webhook",
            "/api/public/**")
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
    return http.build();
  }

  @Bean
  @Order(3)
  public SecurityFilterChain protectedApiFilterChain(HttpSecurity http,
      JwtAuthenticationFilter jwtAuthenticationFilter)
      throws Exception {
    http
        .securityMatcher(
            "/api/**")
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authenticationProvider(authenticationProvider)
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .exceptionHandling(
            exceptions -> exceptions
                .authenticationEntryPoint(restAuthenticationEntryPoint))
        .authorizeHttpRequests(auth -> auth
            .anyRequest().authenticated()
        );
    return http.build();
  }

  @Bean
  @Order(4)
  public SecurityFilterChain webPagesAndGatewayFilterChain(HttpSecurity http,
      JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
    http
        .securityMatcher("/**")
        .csrf(AbstractHttpConfigurer::disable)
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/",
                "/js/**",
                "/css/**",
                "/*.html",
                "/home",
                "/login",
                "/register",
                "/verify-email",
                "/client-register",
                "/verify-2fa",
                "/password/**",
                "/account",
                "/payment/**",
                "/password/**",
                "/paramedic/kyc-form",
                "/paramedic/verify-email",
                "/admin/**",
                "/login/oauth2/success",
                "/paramedic/application-status",
                "/force-change-password").permitAll()
            .anyRequest().authenticated()
        )
        .formLogin(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));
    return http.build();
  }
}