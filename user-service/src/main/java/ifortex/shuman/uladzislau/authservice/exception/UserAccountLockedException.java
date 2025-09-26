package ifortex.shuman.uladzislau.authservice.exception;

public class UserAccountLockedException extends RuntimeException {
    public UserAccountLockedException(String message) {
        super(message);
    }
}