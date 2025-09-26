package ifortex.shuman.uladzislau.authservice.service.permission;

import ifortex.shuman.uladzislau.authservice.dto.UserAuthorizationSnapshot;
import ifortex.shuman.uladzislau.authservice.repository.PermissionRepository;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StaticPermissionContributor implements PermissionContributor {
    private final PermissionRepository permissionRepository;

    @Override
    public Set<String> contributePermissions(UserAuthorizationSnapshot snapshot) {
        return Set.copyOf(permissionRepository.findAllPermissionsByUserId(snapshot.getUserId()));
    }
}