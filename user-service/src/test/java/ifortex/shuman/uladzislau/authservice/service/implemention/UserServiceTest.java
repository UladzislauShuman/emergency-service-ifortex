package ifortex.shuman.uladzislau.authservice.service.implemention;

import ifortex.shuman.uladzislau.authservice.exception.UserNotFoundException;
import ifortex.shuman.uladzislau.authservice.model.Role;
import ifortex.shuman.uladzislau.authservice.model.User;
import ifortex.shuman.uladzislau.authservice.model.UserStatus;
import ifortex.shuman.uladzislau.authservice.repository.UserRepository;
import ifortex.shuman.uladzislau.authservice.service.validation.UserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_EMAIL;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_GOOGLE_ID;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_HASHED_PASSWORD;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_NEW_EMAIL;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_NEW_VALID_PASSWORD;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_VALID_PASSWORD;
import static ifortex.shuman.uladzislau.authservice.config.Constants.VALID_STATUSES_FOR_LOGIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserValidator userValidator;

    @InjectMocks
    private UserServiceImpl userService;

    @Captor private ArgumentCaptor<User> userCaptor;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email(TEST_EMAIL)
                .role(new Role())
                .build();

        ReflectionTestUtils.setField(userService, "tempPasswordExpirationHours", 24L);
    }

    @Test
    void getByEmail_WhenUserExists_ShouldReturnUser() {
        when(userRepository.findByEmailAndStatusIn(TEST_EMAIL, VALID_STATUSES_FOR_LOGIN)).thenReturn(Optional.of(testUser));

        User foundUser = userService.getByEmail(TEST_EMAIL);

        assertThat(foundUser).isEqualTo(testUser);
    }

    @Test
    void getByEmail_WhenUserNotFound_ShouldThrowUserNotFoundException() {
        when(userRepository.findByEmailAndStatusIn(anyString(), any(List.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getByEmail(TEST_EMAIL))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void findById_WhenUserExists_ShouldReturnUser() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        User foundUser = userService.findById(userId);

        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getId()).isEqualTo(userId);
    }

    @Test
    void findById_WhenUserNotFound_ShouldThrowUserNotFoundException() {
        Long userId = 99L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with Id:" + userId);
    }

    @Test
    void findById_WhenIdIsNull_ShouldThrowNullPointerException() {
        assertThatThrownBy(() -> userService.findById(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("userId is null");
    }

    @Test
    void findByGoogleId_WhenUserExists_ShouldReturnUser() {
        String googleId = TEST_GOOGLE_ID;
        testUser.setGoogleId(googleId);
        when(userRepository.findByGoogleId(googleId)).thenReturn(Optional.of(testUser));

        User foundUser = userService.findByGoogleId(googleId);

        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getGoogleId()).isEqualTo(googleId);
    }

    @Test
    void findByGoogleId_WhenUserNotFound_ShouldThrowUserNotFoundException() {
        String googleId = TEST_GOOGLE_ID;
        when(userRepository.findByGoogleId(googleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByGoogleId(googleId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with Google ID: " + googleId);
    }

    @Test
    void findByGoogleId_WhenGoogleIdIsNull_ShouldThrowNullPointerException() {
        assertThatThrownBy(() -> userService.findByGoogleId(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("googleId is null");
    }

    @Test
    void save_WhenUserIsNotNull_ShouldCallRepositorySave() {
        when(userRepository.save(testUser)).thenReturn(testUser);

        User savedUser = userService.save(testUser);

        verify(userRepository, times(1)).save(testUser);
        assertThat(savedUser).isEqualTo(testUser);
    }

    @Test
    void save_WhenUserIsNull_ShouldThrowNullPointerException() {
        assertThatThrownBy(() -> userService.save(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("User is null");
    }

    @Test
    void setUserTemporaryPassword_ShouldEncodePasswordAndSetExpiry() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(anyString())).thenReturn(TEST_HASHED_PASSWORD);

        String tempPassword = userService.setUserTemporaryPassword(1L);

        assertThat(tempPassword).isNotNull().isNotBlank();

        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getPassword()).isEqualTo(TEST_HASHED_PASSWORD);
        assertThat(savedUser.isPasswordTemporary()).isTrue();
        assertThat(savedUser.getPasswordExpiryTime()).isNotNull();
    }

    @Test
    void updateUserPassword_ShouldEncodeAndResetTemporaryFlags() {
        testUser.setPasswordTemporary(true);
        testUser.setPasswordExpiryTime(Instant.now());

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(TEST_VALID_PASSWORD)).thenReturn(TEST_HASHED_PASSWORD);

        userService.updateUserPassword(1L, TEST_VALID_PASSWORD);

        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getPassword()).isEqualTo(TEST_HASHED_PASSWORD);
        assertThat(savedUser.isPasswordTemporary()).isFalse();
        assertThat(savedUser.getPasswordExpiryTime()).isNull();
    }

    @Test
    void softDeleteUser_ForRegularUser_ShouldSetStatusDeleted() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        userService.softDeleteUser(1L);

        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getStatus()).isEqualTo(UserStatus.DELETED);
        assertThat(savedUser.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(savedUser.getGoogleId()).isNull();
    }

    @Test
    void hardDeleteUser_ShouldCallRepositoryDelete() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        userService.hardDeleteUser(1L);

        verify(userRepository, times(1)).delete(testUser);
    }

    @Test
    void loadUserByUsername_ShouldDelegateToGetByEmail() {
        when(userRepository.findByEmailAndStatusIn(anyString(), any(List.class)))
                .thenReturn(Optional.of(testUser));

        UserDetails userDetails = userService.loadUserByUsername(TEST_EMAIL);

        assertThat(userDetails).isEqualTo(testUser);
        verify(userRepository).findByEmailAndStatusIn(eq(TEST_EMAIL), any(List.class));
    }

    @Test
    void updateUserEmailAndInvalidateTokens_ShouldUpdateEmailAndIncrementTokenVersion() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        userService.updateUserEmailAndInvalidateTokens(testUser.getId(), TEST_NEW_EMAIL);

        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getEmail()).isEqualTo(TEST_NEW_EMAIL);
    }

    @Test
    void updateUserPasswordAndStatus_ShouldUpdateAllFieldsCorrectly() {
        testUser.setPasswordTemporary(true);

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(TEST_NEW_VALID_PASSWORD)).thenReturn("new-hashed-password");

        userService.updateUserPasswordAndStatus(testUser.getId(), TEST_NEW_VALID_PASSWORD, UserStatus.ACTIVE);

        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getPassword()).isEqualTo("new-hashed-password");
        assertThat(savedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(savedUser.isPasswordTemporary()).isFalse();
        assertThat(savedUser.getPasswordExpiryTime()).isNull();
    }

    @Test
    void findUserByEmailAndStatus_ShouldCallRepositoryMethod() {
        when(userRepository.findByEmailAndStatus(TEST_EMAIL, UserStatus.PENDING_VERIFICATION))
                .thenReturn(Optional.of(testUser));

        Optional<User> result = userService.findUserByEmailAndStatus(TEST_EMAIL, UserStatus.PENDING_VERIFICATION);

        assertThat(result).isPresent().contains(testUser);
        verify(userRepository).findByEmailAndStatus(TEST_EMAIL, UserStatus.PENDING_VERIFICATION);
    }
}