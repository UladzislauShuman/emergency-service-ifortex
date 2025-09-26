package ifortex.shuman.uladzislau.authservice.config;

import ifortex.shuman.uladzislau.authservice.model.User;
import ifortex.shuman.uladzislau.authservice.service.JwtService;
import ifortex.shuman.uladzislau.authservice.service.UserTokenService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;
  private final UserTokenService userTokenService;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain
  ) throws ServletException, IOException {
    try {
      Optional<String> tokenOpt = extractTokenFromRequest(request);
      tokenOpt.ifPresent(token -> {
        String userEmail = jwtService.extractUsername(token);
        if (userEmail != null && !isAuthenticated()) {
          authenticateUser(request, token, userEmail);
        }
      });
    } catch (JwtException e) {
      log.debug("Invalid JWT Token received: {}", e.getMessage());
    }

    filterChain.doFilter(request, response);
  }

  private boolean isAuthenticated() {
    return SecurityContextHolder.getContext().getAuthentication() != null;
  }

  private void authenticateUser(HttpServletRequest request, String token, String userEmail) {
    UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

    if (!isRefreshTokenValid((User) userDetails)) {
      log.warn("FILTER (JWT): No valid refresh token found for user '{}'. Access denied.",
          userEmail);
      return;
    }

    if (jwtService.isTokenValid(token, userDetails)) {
      List<String> authoritiesFromToken = jwtService.extractClaim(token,
          claims -> claims.get("authorities", List.class));
      List<SimpleGrantedAuthority> authorities = authoritiesFromToken.stream()
          .map(SimpleGrantedAuthority::new)
          .collect(Collectors.toList());
      UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
          userDetails,
          null,
          authorities
      );
      authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      SecurityContextHolder.getContext().setAuthentication(authToken);
    } else {
      log.warn("FILTER (JWT): Token validation failed for user '{}'.", userEmail);
    }
  }

  private boolean isRefreshTokenValid(User user) {
    try {
      return userTokenService.hasActiveRefreshToken(user);
    } catch (Exception e) {
      log.error("Error checking refresh token validity for user {}", user.getEmail(), e);
      return false;
    }
  }

  private Optional<String> extractTokenFromRequest(HttpServletRequest request) {
    final String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      return Optional.of(authHeader.substring("Bearer ".length()));
    }

    if ("/profile/link-google".equals(request.getRequestURI())) {
      String tokenFromParam = request.getParameter("token");
      if (tokenFromParam != null && !tokenFromParam.isBlank()) {
        return Optional.of(tokenFromParam);
      }
    }

    return Optional.empty();
  }
}