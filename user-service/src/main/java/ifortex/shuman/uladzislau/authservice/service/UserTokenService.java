package ifortex.shuman.uladzislau.authservice.service;

import ifortex.shuman.uladzislau.authservice.model.TokenType;
import ifortex.shuman.uladzislau.authservice.model.User;
import ifortex.shuman.uladzislau.authservice.model.UserToken;

public interface UserTokenService {

  UserToken save(UserToken userToken);

  void delete(UserToken userToken);

  void deleteAllByUserAndType(User user, TokenType type);

  UserToken findByTokenAndType(String token, TokenType type);

  void saveUserRefreshToken(User user, String token);

  String createPasswordResetToken(User user);

  UserToken validateAndRetrieveToken(String token, TokenType type);

  boolean hasActiveRefreshToken(User user);
}
