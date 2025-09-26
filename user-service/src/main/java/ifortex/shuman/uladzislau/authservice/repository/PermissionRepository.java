package ifortex.shuman.uladzislau.authservice.repository;

import ifortex.shuman.uladzislau.authservice.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PermissionRepository extends JpaRepository<Permission, Integer> {

  @Query(value = """ 
      (
             SELECT p.name
             FROM permissions p
             JOIN roles_permissions rp ON p.id = rp.permission_id
             JOIN users u ON rp.role_id = u.role_id
             WHERE u.id = :userId
      )
      """, nativeQuery = true)
  List<String> findAllPermissionsByUserId(@Param("userId") Long userId);
}