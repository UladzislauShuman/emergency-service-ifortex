package ifortex.shuman.uladzislau.authservice.service.permission;

import ifortex.shuman.uladzislau.authservice.dto.UserAuthorizationSnapshot;
import ifortex.shuman.uladzislau.authservice.model.UserRole;
import ifortex.shuman.uladzislau.authservice.repository.PermissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StaticPermissionContributorTest {

  @Mock
  private PermissionRepository permissionRepository;

  @InjectMocks
  private StaticPermissionContributor staticPermissionContributor;

  private UserAuthorizationSnapshot userSnapshot;

  @BeforeEach
  void setUp() {
    userSnapshot = new UserAuthorizationSnapshot(123L, UserRole.ROLE_ADMIN, false, null);
  }

  @Test
  void contributePermissions_shouldReturnPermissionsFromRepository() {
    List<String> permissionsFromDb = List.of("admin:user:read", "admin:user:update");
    when(permissionRepository.findAllPermissionsByUserId(123L)).thenReturn(permissionsFromDb);

    Set<String> actualPermissions = staticPermissionContributor.contributePermissions(userSnapshot);

    assertThat(actualPermissions).containsExactlyInAnyOrder("admin:user:read", "admin:user:update");
    verify(permissionRepository).findAllPermissionsByUserId(123L);
  }

  @Test
  void contributePermissions_shouldReturnEmptySetWhenRepositoryReturnsEmpty() {
    when(permissionRepository.findAllPermissionsByUserId(123L)).thenReturn(Collections.emptyList());

    Set<String> actualPermissions = staticPermissionContributor.contributePermissions(userSnapshot);

    assertThat(actualPermissions).isEmpty();
  }

  @Test
  void contributePermissions_shouldHandleNullSnapshotGracefully() {
    assertThatThrownBy(() -> staticPermissionContributor.contributePermissions(null))
        .isInstanceOf(NullPointerException.class);
  }
}