package ifortex.shuman.uladzislau.authservice.service.implemention;

import ifortex.shuman.uladzislau.authservice.dto.HomePageDetailsDto;
import ifortex.shuman.uladzislau.authservice.dto.UserDto;
import ifortex.shuman.uladzislau.authservice.model.Role;
import ifortex.shuman.uladzislau.authservice.model.User;
import ifortex.shuman.uladzislau.authservice.model.UserRole;
import ifortex.shuman.uladzislau.authservice.paramedic.dto.KycApplicationSummaryDto;
import ifortex.shuman.uladzislau.authservice.paramedic.model.ParamedicApplication;
import ifortex.shuman.uladzislau.authservice.paramedic.model.ParamedicApplicationStatus;
import ifortex.shuman.uladzislau.authservice.paramedic.repository.ParamedicApplicationRepository;
import ifortex.shuman.uladzislau.authservice.service.permission.PermissionService;
import ifortex.shuman.uladzislau.authservice.util.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HomePageServiceImplTest {

  @Mock
  private UserMapper userMapper;
  @Mock
  private PermissionService permissionService;
  @Mock
  private ParamedicApplicationRepository paramedicApplicationRepository;

  @InjectMocks
  private HomePageServiceImpl homePageService;

  private User clientUser;
  private User paramedicUser;

  @BeforeEach
  void setUp() {
    Role clientRole = new Role();
    clientRole.setName(UserRole.ROLE_CLIENT);
    clientUser = new User();
    clientUser.setId(1L);
    clientUser.setRole(clientRole);

    Role paramedicRole = new Role();
    paramedicRole.setName(UserRole.ROLE_PARAMEDIC);
    paramedicUser = new User();
    paramedicUser.setId(2L);
    paramedicUser.setRole(paramedicRole);
  }

  @Test
  void getHomePageDetails_whenUserIsClient_shouldNotQueryForKycApplication() {
    UserDto userDto = UserDto.builder().id(clientUser.getId()).build();
    Set<String> permissions = Set.of("some:permission");

    when(userMapper.toUserDto(clientUser)).thenReturn(userDto);
    when(permissionService.calculatePermissionsForUser(clientUser)).thenReturn(permissions);

    HomePageDetailsDto result = homePageService.getHomePageDetails(clientUser);

    assertThat(result).isNotNull();
    assertThat(result.getUser()).isEqualTo(userDto);
    assertThat(result.getPermissions()).isEqualTo(permissions);
    assertThat(result.getKycApplication()).isNull();
    verifyNoInteractions(paramedicApplicationRepository);
  }

  @Test
  void getHomePageDetails_whenUserIsParamedicWithApplication_shouldIncludeKycSummary() {
    UserDto userDto = UserDto.builder().id(paramedicUser.getId()).build();
    Set<String> permissions = Set.of("paramedic:permission");
    ParamedicApplication application = new ParamedicApplication();
    application.setStatus(ParamedicApplicationStatus.PENDING_REVIEW);
    application.setSubmittedAt(Instant.now());

    when(userMapper.toUserDto(paramedicUser)).thenReturn(userDto);
    when(permissionService.calculatePermissionsForUser(paramedicUser)).thenReturn(permissions);

    HomePageDetailsDto result = homePageService.getHomePageDetails(paramedicUser);

    assertThat(result).isNotNull();
    assertThat(result.getUser()).isEqualTo(userDto);
    assertThat(result.getPermissions()).isEqualTo(permissions);

    KycApplicationSummaryDto kycSummary = result.getKycApplication();
    assertThat(kycSummary).isNotNull();
    assertThat(kycSummary.getStatus()).isEqualTo(ParamedicApplicationStatus.PENDING_REVIEW);
    assertThat(kycSummary.getSubmittedAt()).isEqualTo(application.getSubmittedAt());
  }
}