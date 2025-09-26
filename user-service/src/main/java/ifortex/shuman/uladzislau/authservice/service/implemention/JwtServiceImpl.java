package ifortex.shuman.uladzislau.authservice.service.implemention;

import ifortex.shuman.uladzislau.authservice.config.properties.JwtProperties;
import ifortex.shuman.uladzislau.authservice.model.User;
import ifortex.shuman.uladzislau.authservice.service.JwtService;
import ifortex.shuman.uladzislau.authservice.service.permission.PermissionService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtServiceImpl implements JwtService {

  private final JwtProperties jwtProperties;
  private final PermissionService permissionService;

  @Override
  public String extractUsername(String token) {
    return extractClaim(token, claims -> claims.get("email", String.class));
  }

  @Override
  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  @Override
  public String generateToken(User user) {
    Map<String, Object> extraClaims = new HashMap<>();
    Set<String> authorities = permissionService.calculatePermissionsForUser(user);
    extraClaims.put("authorities", authorities);
    return buildToken(extraClaims, user, jwtProperties.getAccessToken().getExpiration());
  }

  @Override
  public String generateRefreshToken(User user) {
    return buildToken(new HashMap<>(), user, jwtProperties.getRefreshToken().getExpiration());
  }

  @Override
  public boolean isTokenValid(String token, UserDetails userDetails) {
    try {
      final String username = extractUsername(token);
      return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    } catch (JwtException e) {
      log.debug("Token is invalid: {}", e.getMessage());
      return false;
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public Claims extractAllClaims(String token) {
    return Jwts
        .parserBuilder()
        .setSigningKey(getSignInKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  private String buildToken(Map<String, Object> extraClaims, User user, long expiration) {
    extraClaims.put("email", user.getEmail());
    return Jwts
        .builder()
        .setClaims(extraClaims)
        .setSubject(user.getId().toString())
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(getSignInKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  private boolean isTokenExpired(String token) {
    try {
      return extractExpiration(token).before(new Date());
    } catch (ExpiredJwtException e) {
      return true;
    }
  }

  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  private Key getSignInKey() {
    byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecretKey());
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
