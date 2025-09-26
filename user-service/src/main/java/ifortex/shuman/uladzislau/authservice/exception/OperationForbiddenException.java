package ifortex.shuman.uladzislau.authservice.exception;

public class OperationForbiddenException extends RuntimeException {
    public OperationForbiddenException(String message) {
        super(message);
    }
}