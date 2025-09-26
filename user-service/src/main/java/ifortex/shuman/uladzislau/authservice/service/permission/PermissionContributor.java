package ifortex.shuman.uladzislau.authservice.service.permission;

import ifortex.shuman.uladzislau.authservice.dto.UserAuthorizationSnapshot;
import java.util.Set;

public interface PermissionContributor {

  Set<String> contributePermissions(UserAuthorizationSnapshot snapshot);
}