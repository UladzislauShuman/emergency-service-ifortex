package ifortex.shuman.uladzislau.authservice.repository;

import ifortex.shuman.uladzislau.authservice.model.User;
import ifortex.shuman.uladzislau.authservice.model.UserStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

  @EntityGraph(attributePaths = "role.permissions")
  Optional<User> findByEmailAndStatusIn(String email, List<UserStatus> statuses);

  @EntityGraph(attributePaths = "role")
  Optional<User> findByGoogleId(String googleId);

  Optional<User> findByEmailAndStatus(String email, UserStatus status);

  boolean existsByEmail(String email);
}