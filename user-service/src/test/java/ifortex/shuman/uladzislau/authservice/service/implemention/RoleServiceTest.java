package ifortex.shuman.uladzislau.authservice.service.implemention;

import ifortex.shuman.uladzislau.authservice.model.Role;
import ifortex.shuman.uladzislau.authservice.model.UserRole;
import ifortex.shuman.uladzislau.authservice.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    @Test
    void getDefaultClientRole_WhenRoleExists_ShouldReturnRole() {
        Role clientRole = new Role();
        clientRole.setName(UserRole.ROLE_CLIENT);

        when(roleRepository.findByName(UserRole.ROLE_CLIENT)).thenReturn(Optional.of(clientRole));

        Role result = roleService.getDefaultClientRole();

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(UserRole.ROLE_CLIENT);
    }

    @Test
    void getDefaultClientRole_WhenRoleNotFound_ShouldThrowIllegalStateException() {
        when(roleRepository.findByName(UserRole.ROLE_CLIENT)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.getDefaultClientRole())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Default role 'ROLE_CLIENT' not found. Please initialize roles.");
    }

    @Test
    void findByName_WhenRoleExists_ShouldReturnRole() {
        UserRole roleToFind = UserRole.ROLE_ADMIN;
        Role adminRole = new Role();
        adminRole.setName(roleToFind);

        when(roleRepository.findByName(roleToFind)).thenReturn(Optional.of(adminRole));

        Role result = roleService.findByName(roleToFind);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(roleToFind);
    }

    @Test
    void findByName_WhenRoleNotFound_ShouldThrowIllegalStateException() {
        UserRole roleToFind = UserRole.ROLE_PARAMEDIC;
        when(roleRepository.findByName(roleToFind)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.findByName(roleToFind))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Role " + roleToFind + " not found.");
    }
}