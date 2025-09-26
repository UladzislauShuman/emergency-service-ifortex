package ifortex.shuman.uladzislau.authservice.service.permission;

import ifortex.shuman.uladzislau.authservice.dto.UserAuthorizationSnapshot;
import ifortex.shuman.uladzislau.authservice.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DynamicPermissionService implements PermissionService {

  private final List<PermissionContributor> contributors;

  @Override
  public Set<String> calculatePermissionsForUser(User user) {
    UserAuthorizationSnapshot snapshot = new UserAuthorizationSnapshot(
        user.getId(),
        user.getRole().getName(),
        false,
        user.getStatus()
    );

    return contributors.stream()
        .flatMap(contributor -> contributor.contributePermissions(snapshot).stream())
        .collect(Collectors.toSet());
  }
}