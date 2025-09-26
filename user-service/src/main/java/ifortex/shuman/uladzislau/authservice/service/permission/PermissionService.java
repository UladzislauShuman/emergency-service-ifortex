package ifortex.shuman.uladzislau.authservice.service.permission;

import ifortex.shuman.uladzislau.authservice.model.User;
import java.util.Set;

public interface PermissionService {
  Set<String> calculatePermissionsForUser(User user);
}
