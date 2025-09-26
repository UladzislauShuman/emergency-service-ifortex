package ifortex.shuman.uladzislau.authservice.service;

import ifortex.shuman.uladzislau.authservice.model.User;
import ifortex.shuman.uladzislau.authservice.model.UserStatus;

import java.util.Optional;

public interface UserService {

    User getByEmail(String email);
    String generateRandomPassword();
    User findByGoogleId(String googleId);
    User findById(Long userId);
    User save(User user);
    String setUserTemporaryPassword(Long userId);
    void updateUserPassword(Long userId, String newRawPassword);
    void updateUserEmailAndInvalidateTokens(Long userId, String newEmail);
    void updateUserPasswordAndStatus(Long userId, String newRawPassword, UserStatus newStatus);
    Optional<User> findUserByEmailAndStatus(String email, UserStatus status);
    void softDeleteUser(Long userId);
    void hardDeleteUser(Long userId);
}