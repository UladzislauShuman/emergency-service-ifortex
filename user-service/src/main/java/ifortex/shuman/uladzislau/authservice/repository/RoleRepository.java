package ifortex.shuman.uladzislau.authservice.repository;

import ifortex.shuman.uladzislau.authservice.model.Role;
import ifortex.shuman.uladzislau.authservice.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(UserRole name);
}