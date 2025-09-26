package ifortex.shuman.uladzislau.authservice.service.implemention;

import ifortex.shuman.uladzislau.authservice.model.Role;
import ifortex.shuman.uladzislau.authservice.model.UserRole;
import ifortex.shuman.uladzislau.authservice.repository.RoleRepository;
import ifortex.shuman.uladzislau.authservice.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;

    @Override
    public Role getDefaultClientRole() {
        return roleRepository.findByName(UserRole.ROLE_CLIENT)
                .orElseThrow(() ->
                        new IllegalStateException("Default role 'ROLE_CLIENT' not found. Please initialize roles."));
    }

    @Override
    public Role findByName(UserRole name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new IllegalStateException("Role " + name + " not found."));
    }
}
