package ifortex.shuman.uladzislau.authservice.service.implemention;

import ifortex.shuman.uladzislau.authservice.config.properties.FrontendProperties;
import ifortex.shuman.uladzislau.authservice.dto.UpdateUserByAdminRequestDto;
import ifortex.shuman.uladzislau.authservice.dto.UserDto;
import ifortex.shuman.uladzislau.authservice.model.Role;
import ifortex.shuman.uladzislau.authservice.model.User;
import ifortex.shuman.uladzislau.authservice.model.UserRole;
import ifortex.shuman.uladzislau.authservice.model.UserStatus;
import ifortex.shuman.uladzislau.authservice.repository.UserRepository;
import ifortex.shuman.uladzislau.authservice.repository.specification.UserSpecification;
import ifortex.shuman.uladzislau.authservice.service.*;
import ifortex.shuman.uladzislau.authservice.service.validation.UserValidator;
import ifortex.shuman.uladzislau.authservice.util.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.access.AccessDeniedException;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_EMAIL;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_FULL_NAME;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_PHONE;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_SUPER_ADMIN_EMAIL;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_UPDATE_USER_FULL_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private UserMapper userMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleService roleService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserService userService;
    @Mock
    private UserTokenService userTokenService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private UserValidator userValidator;
    @Mock
    private UserSpecification userSpecification;
    @Mock
    private Page<User> userPage;
    @Mock
    private Pageable pageable;
    @Mock
    private FrontendProperties frontendProperties;

    @InjectMocks
    private AdminUserServiceImpl adminUserService;

    @Captor
    private ArgumentCaptor<User> userArgumentCaptor;

    private User existingUser;
    private User superAdminUser;

    @BeforeEach
    void setUp() {
        existingUser = User.builder()
                .id(10L)
                .email(TEST_EMAIL)
                .fullName(TEST_FULL_NAME)
                .phone(TEST_PHONE)
                .role(new Role())
                .build();

        Role superAdminRole = new Role();
        superAdminRole.setName(UserRole.ROLE_SUPER_ADMIN);
        superAdminUser = User.builder()
                .id(1L)
                .email(TEST_SUPER_ADMIN_EMAIL)
                .role(superAdminRole)
                .build();
    }

    @Test
    void updateUserByAdmin_WhenUserIsNotSuperAdmin_ShouldUpdateFields() {
        Long userId = existingUser.getId();
        UpdateUserByAdminRequestDto updateRequest = new UpdateUserByAdminRequestDto();
        updateRequest.setFullName(TEST_UPDATE_USER_FULL_NAME);
        updateRequest.setPhone(TEST_PHONE);

        when(userService.findById(userId)).thenReturn(existingUser);
        when(userService.save(any(User.class))).thenReturn(existingUser);
        when(userMapper.toUserDto(any(User.class))).thenReturn(UserDto.builder().build());

        adminUserService.updateUserByAdmin(userId, updateRequest);

        verify(userService).save(userArgumentCaptor.capture());
        User savedUser = userArgumentCaptor.getValue();
        assertThat(savedUser.getFullName()).isEqualTo(TEST_UPDATE_USER_FULL_NAME);
        assertThat(savedUser.getPhone()).isEqualTo(TEST_PHONE);
    }

    @Test
    void updateUserByAdmin_WhenUserIsSuperAdmin_ShouldThrowException() {

        when(userService.findById(superAdminUser.getId())).thenReturn(superAdminUser);

        doThrow(new IllegalArgumentException("Cannot update a super admin account."))
                .when(userValidator).ensureNotSuperAdmin(superAdminUser);

        assertThatThrownBy(() -> adminUserService.updateUserByAdmin(superAdminUser.getId(),
                new UpdateUserByAdminRequestDto()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot update a super admin account.");
    }

    @Test
    void unblockUser_Permanently_ShouldSetStatusToActive() {
        Long userId = existingUser.getId();
        //existingUser.setStatus(UserStatus.BLOCKED);
        when(userService.findById(userId)).thenReturn(existingUser);

        //adminUserService.unblockUser(userId);

        verify(userService).save(userArgumentCaptor.capture());
        User savedUser = userArgumentCaptor.getValue();

        assertThat(savedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void softDeleteUser_ShouldCallUserService() {
        Long userId = existingUser.getId();

        adminUserService.softDeleteUser(userId, existingUser);

        verify(userService, times(1)).softDeleteUser(userId);
    }

    @Test
    void hardDeleteUser_ShouldCallUserService() {
        Long userId = existingUser.getId();

        adminUserService.hardDeleteUser(userId, existingUser);

        verify(userService, times(1)).hardDeleteUser(userId);
    }

    @Test
    void updateUserByAdmin_WithPartialData_ShouldUpdateOnlyProvidedFields() {
        Long userId = existingUser.getId();
        String originalPhone = existingUser.getPhone();

        UpdateUserByAdminRequestDto updateRequest = new UpdateUserByAdminRequestDto();
        updateRequest.setFullName(TEST_UPDATE_USER_FULL_NAME);
        updateRequest.setPhone(null);

        when(userService.findById(userId)).thenReturn(existingUser);
        when(userService.save(any(User.class))).thenReturn(existingUser);
        when(userMapper.toUserDto(any(User.class))).thenReturn(UserDto.builder().build());

        adminUserService.updateUserByAdmin(userId, updateRequest);


        verify(userService).save(userArgumentCaptor.capture());
        User savedUser = userArgumentCaptor.getValue();
        assertThat(savedUser.getFullName()).isEqualTo(TEST_UPDATE_USER_FULL_NAME);
        assertThat(savedUser.getPhone()).isEqualTo(originalPhone);
    }

    @Test
    void findUsersByComplexFilter_ShouldCallRepositoryWithCorrectlyBuiltSpecification() {
        Set<String> filters = Set.of("ROLE_CLIENT", "BLOCKED");
        String fullName = "Test";
        Pageable pageable = Pageable.unpaged();

        when(userRepository.findAll(any(Specification.class), Mockito.eq(pageable)))
                .thenReturn(Page.empty());

        adminUserService.findUsersByComplexFilter(null, pageable);

        verify(userRepository).findAll(any(Specification.class), Mockito.eq(pageable));
    }
}