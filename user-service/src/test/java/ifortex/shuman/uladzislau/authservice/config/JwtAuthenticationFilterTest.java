package ifortex.shuman.uladzislau.authservice.config;

import ifortex.shuman.uladzislau.authservice.model.User;
import ifortex.shuman.uladzislau.authservice.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.impl.DefaultClaims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

  @Mock
  private JwtService jwtService;
  @Mock
  private UserDetailsService userDetailsService;
  @Mock
  private FilterChain filterChain;

  @InjectMocks
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  private MockHttpServletRequest request;
  private MockHttpServletResponse response;
  private User testUser;

  @BeforeEach
  void setUp() {
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    testUser = new User();
    testUser.setId(1L);
    testUser.setEmail("test@example.com");
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void doFilterInternal_whenValidToken_shouldAuthenticateUser() throws ServletException, IOException {
    String token = "valid-token";
    request.addHeader("Authorization", "Bearer " + token);

    when(jwtService.extractUsername(token)).thenReturn(testUser.getEmail());
    when(userDetailsService.loadUserByUsername(testUser.getEmail())).thenReturn(testUser);

    Claims claims = new DefaultClaims();
    claims.put("token_version", 0);
    claims.put("authorities", List.of("ROLE_USER"));
    when(jwtService.extractClaim(eq(token), any())).thenAnswer(invocation -> {
      Function<Claims, ?> resolver = invocation.getArgument(1);
      return resolver.apply(claims);
    });

    when(jwtService.isTokenValid(token, testUser)).thenReturn(true);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo(testUser.getEmail());
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void doFilterInternal_whenNoAuthorizationHeader_shouldNotAuthenticate() throws ServletException, IOException {
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(jwtService, userDetailsService);
  }

  @Test
  void doFilterInternal_whenAuthorizationHeaderIsIncorrect_shouldNotAuthenticate() throws ServletException, IOException {
    request.addHeader("Authorization", "Token invalid-token");

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(jwtService, userDetailsService);
  }

  @Test
  void doFilterInternal_whenTokenIsInvalid_shouldNotAuthenticate() throws ServletException, IOException {
    String token = "invalid-token";
    request.addHeader("Authorization", "Bearer " + token);

    when(jwtService.extractUsername(token)).thenReturn(testUser.getEmail());
    when(userDetailsService.loadUserByUsername(testUser.getEmail())).thenReturn(testUser);

    Claims claims = new DefaultClaims();
    claims.put("token_version", 0);
    when(jwtService.extractClaim(eq(token), any())).thenAnswer(invocation -> {
      Function<Claims, ?> resolver = invocation.getArgument(1);
      return resolver.apply(claims);
    });

    when(jwtService.isTokenValid(token, testUser)).thenReturn(false);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void doFilterInternal_whenTokenVersionMismatch_shouldNotAuthenticate() throws ServletException, IOException {
    String token = "outdated-token";
    request.addHeader("Authorization", "Bearer " + token);

    when(jwtService.extractUsername(token)).thenReturn(testUser.getEmail());
    when(userDetailsService.loadUserByUsername(testUser.getEmail())).thenReturn(testUser);

    Claims claims = new DefaultClaims();
    claims.put("token_version", 0);
    when(jwtService.extractClaim(eq(token), any())).thenAnswer(invocation -> {
      Function<Claims, ?> resolver = invocation.getArgument(1);
      return resolver.apply(claims);
    });

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(jwtService, never()).isTokenValid(any(), any());
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void doFilterInternal_whenUserAlreadyAuthenticated_shouldNotReAuthenticate() throws ServletException, IOException {
    String token = "valid-token";
    request.addHeader("Authorization", "Bearer " + token);
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken("already.auth@user.com", null)
    );

    when(jwtService.extractUsername(token)).thenReturn("some.user@example.com");

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(jwtService).extractUsername(token);
    verifyNoMoreInteractions(jwtService);
    verifyNoInteractions(userDetailsService);
    verify(filterChain).doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("already.auth@user.com");
  }

  @Test
  void doFilterInternal_whenJwtExceptionOccurs_shouldNotAuthenticateAndContinueFilterChain() throws ServletException, IOException {
    String token = "exception-token";
    request.addHeader("Authorization", "Bearer " + token);

    when(jwtService.extractUsername(token)).thenThrow(new JwtException("Invalid signature"));

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(userDetailsService);
  }

  @Test
  void doFilterInternal_whenUsernameFromTokenIsNull_shouldNotAuthenticate() throws ServletException, IOException {
    String token = "token-with-null-user";
    request.addHeader("Authorization", "Bearer " + token);

    when(jwtService.extractUsername(token)).thenReturn(null);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(userDetailsService);
  }

  @Test
  void doFilterInternal_whenUserNotFound_shouldThrowExceptionAndNotAuthenticate() throws ServletException, IOException {
    String token = "token-for-nonexistent-user";
    String userEmail = "nonexistent@example.com";
    request.addHeader("Authorization", "Bearer " + token);

    when(jwtService.extractUsername(token)).thenReturn(userEmail);
    when(userDetailsService.loadUserByUsername(userEmail))
        .thenThrow(new UsernameNotFoundException("User not found"));

    try {
      jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
    } catch (UsernameNotFoundException e) {}

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain, never()).doFilter(request, response);
  }
}
