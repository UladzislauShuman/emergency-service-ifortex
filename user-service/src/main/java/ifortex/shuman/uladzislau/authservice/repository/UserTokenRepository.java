package ifortex.shuman.uladzislau.authservice.repository;// в пакете repository


import ifortex.shuman.uladzislau.authservice.model.User;
import ifortex.shuman.uladzislau.authservice.model.UserToken;
import ifortex.shuman.uladzislau.authservice.model.TokenType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserTokenRepository extends JpaRepository<UserToken, Long> {

  Optional<UserToken> findByTokenAndType(String token, TokenType type);

  void deleteAllByUserAndType(User user, TokenType type);

  boolean existsByUserAndType(User user, TokenType type);
}