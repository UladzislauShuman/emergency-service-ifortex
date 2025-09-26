package ifortex.shuman.uladzislau.billing.exception;

// for state conflicts (e.g. when you want to block user who has already blocked)
public class ResourceConflictException extends RuntimeException {
    public ResourceConflictException(String message) {
        super(message);
    }
}