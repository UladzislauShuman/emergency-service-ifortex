package ifortex.shuman.uladzislau.authservice.service.validation;

import ifortex.shuman.uladzislau.authservice.model.Role;
import ifortex.shuman.uladzislau.authservice.model.User;
import ifortex.shuman.uladzislau.authservice.model.UserRole;
import ifortex.shuman.uladzislau.authservice.model.UserStatus;
import ifortex.shuman.uladzislau.authservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_EMAIL;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_HASHED_PASSWORD;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_VALID_PASSWORD;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_WRONG_PASSWORD;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserValidatorTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserValidator userValidator;

    private User testUser;
    private User adminUser;
    private User superAdminUser;

    @BeforeEach
    void setUp() {
        Role clientRole = new Role();
        clientRole.setName(UserRole.ROLE_CLIENT);
        testUser = User.builder()
                .id(1L)
                .status(UserStatus.ACTIVE)
                .password(TEST_HASHED_PASSWORD)
                .role(clientRole)
                .build();

        Role adminRole = new Role();
        adminRole.setName(UserRole.ROLE_ADMIN);
        adminUser = User.builder()
                .id(2L)
                .status(UserStatus.ACTIVE)
                .role(adminRole)
                .build();

        Role superAdminRole = new Role();
        superAdminRole.setName(UserRole.ROLE_SUPER_ADMIN);
        superAdminUser = User.builder()
                .id(3L)
                .status(UserStatus.ACTIVE)
                .role(superAdminRole)
                .build();
    }

    @Test
    void validateEmailIsAvailable_WhenEmailIsFree_ShouldNotThrowException() {
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
        assertDoesNotThrow(() -> userValidator.validateEmailIsAvailable(TEST_EMAIL));
    }

    @Test
    void validateEmailIsAvailable_WhenEmailExists_ShouldThrowException() {
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(true);
        assertThatThrownBy(() -> userValidator.validateEmailIsAvailable(TEST_EMAIL))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void ensureIsNotBlockingHisSelf_WhenIdsAreDifferent_ShouldNotThrowException() {
        User currentUser = User.builder().id(1L).build();
        Long otherUserId = 2L;
        assertDoesNotThrow(() -> userValidator.ensureIsNotBlockingOrDeleteHimSelf(currentUser, otherUserId));
    }

    @Test
    void ensureIsNotBlockingHimSelf_WhenIdsAreEqual_ShouldThrowException() {
        User currentUser = User.builder().id(1L).build();
        Long selfId = 1L;
        assertThatThrownBy(() -> userValidator.ensureIsNotBlockingOrDeleteHimSelf(currentUser, selfId))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void ensureNotSuperAdmin_WhenUserIsRegular_ShouldNotThrowException() {
        assertDoesNotThrow(() -> userValidator.ensureNotSuperAdmin(testUser));
    }

    @Test
    void ensureNotSuperAdmin_WhenUserIsSuperAdmin_ShouldThrowException() {
        assertThatThrownBy(() -> userValidator.ensureNotSuperAdmin(superAdminUser))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void validatePasswordConfirmation_WhenPasswordsMatch_ShouldNotThrowException() {
        assertDoesNotThrow(() -> userValidator.validatePasswordConfirmation("pass123", "pass123"));
    }

    @Test
    void validatePasswordConfirmation_WhenPasswordsDoNotMatch_ShouldThrowException() {
        assertThatThrownBy(() -> userValidator.validatePasswordConfirmation("pass123", "pass456"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void checkPasswordMatch_WhenPasswordsMatch_ShouldNotThrowException() {
        when(passwordEncoder.matches(TEST_VALID_PASSWORD, TEST_HASHED_PASSWORD)).thenReturn(true);
        assertDoesNotThrow(() -> userValidator.checkPasswordMatch(testUser, TEST_VALID_PASSWORD));
    }

    @Test
    void checkPasswordMatch_WhenPasswordsDoNotMatch_ShouldThrowException() {
        when(passwordEncoder.matches(TEST_WRONG_PASSWORD, TEST_HASHED_PASSWORD)).thenReturn(false);
        assertThatThrownBy(() -> userValidator.checkPasswordMatch(testUser, TEST_WRONG_PASSWORD))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void whenUserIsActive_shouldNotThrowException() {
        testUser.setStatus(UserStatus.ACTIVE);
        assertDoesNotThrow(() -> userValidator.ensureUserIsActive(testUser));
    }

    @Test
    void whenUserIsBlocked_shouldThrowAccessDeniedException() {
       // testUser.setStatus(UserStatus.BLOCKED);
        assertThatThrownBy(() -> userValidator.ensureUserIsActive(testUser))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void whenUserIsAdmin_shouldNotThrowException() {
        assertDoesNotThrow(() -> userValidator.ensureUserHasAdminRights(adminUser));
    }

    @Test
    void whenUserIsSuperAdmin_shouldNotThrowException() {
        assertDoesNotThrow(() -> userValidator.ensureUserHasAdminRights(superAdminUser));
    }

    @Test
    void whenUserIsClient_shouldThrowAccessDeniedException() {
        assertThatThrownBy(() -> userValidator.ensureUserHasAdminRights(testUser))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void whenSuperAdminBlocksAdmin_shouldNotThrowException() {
        assertDoesNotThrow(() -> userValidator.canBlockOrDeleteUser(superAdminUser, adminUser));
    }

    @Test
    void whenAdminBlocksClient_shouldNotThrowException() {
        assertDoesNotThrow(() -> userValidator.canBlockOrDeleteUser(adminUser, testUser));
    }

    @Test
    void whenAdminBlocksAdmin_shouldThrowAccessDeniedException() {
        User otherAdmin = User.builder().id(4L).role(adminUser.getRole()).build();
        assertThatThrownBy(() -> userValidator.canBlockOrDeleteUser(adminUser, otherAdmin))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("he cant block or delete user");
    }

    @Test
    void whenAdminBlocksSuperAdmin_shouldThrowAccessDeniedException() {
        assertThatThrownBy(() -> userValidator.canBlockOrDeleteUser(adminUser, superAdminUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("he cant block or delete user");
    }

    @Test
    void whenBlockingAdminIsNotActive_shouldThrowAccessDeniedException() {
        //adminUser.setStatus(UserStatus.BLOCKED);
        assertThatThrownBy(() -> userValidator.canBlockOrDeleteUser(adminUser, testUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("is not active");
    }

    @Test
    void whenClientTriesToBlock_shouldThrowAccessDeniedException() {
        assertThatThrownBy(() -> userValidator.canBlockOrDeleteUser(testUser, adminUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("does not have admin rights");
    }
}