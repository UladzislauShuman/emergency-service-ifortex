package ifortex.shuman.uladzislau.authservice.service;

import ifortex.shuman.uladzislau.authservice.model.User;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.function.Function;

public interface JwtService {
    String extractUsername(String token);
    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);
    String generateToken(User user);
    String generateRefreshToken(User user);
    boolean isTokenValid(String token, UserDetails userDetails);
    Claims extractAllClaims(String token);
}
