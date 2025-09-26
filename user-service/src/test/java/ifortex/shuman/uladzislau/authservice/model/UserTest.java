package ifortex.shuman.uladzislau.authservice.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void isAccountNonLocked_WhenTemporarilyBlockedAndNotExpired_ShouldReturnFalse() {
        User user = User.builder()
                //.status(UserStatus.BLOCKED)
                .blockedUntil(Instant.now().plus(1, ChronoUnit.HOURS))
                .build();

        assertThat(user.isAccountNonLocked()).isFalse();
    }

    @Test
    void isAccountNonLocked_WhenStatusIsBlocked_ShouldReturnFalse() {
        User user = User.builder()
                //.status(UserStatus.BLOCKED)
                .build();

        assertThat(user.isAccountNonLocked()).isFalse();
    }

    @Test
    void isAccountNonLocked_WhenStatusIsActive_ShouldReturnTrue() {
        User user = User.builder()
                .status(UserStatus.ACTIVE)
                .build();

        assertThat(user.isAccountNonLocked()).isTrue();
    }

    @Test
    void isAccountNonLocked_WhenStatusIsPasswordResetPending_ShouldReturnFalse() {
        User user = User.builder()
                .status(UserStatus.PASSWORD_RESET_PENDING)
                .build();

        assertThat(user.isAccountNonLocked()).isFalse();
    }
}