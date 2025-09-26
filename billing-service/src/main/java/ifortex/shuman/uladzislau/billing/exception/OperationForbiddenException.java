package ifortex.shuman.uladzislau.billing.exception;

public class OperationForbiddenException extends RuntimeException {
    public OperationForbiddenException(String message) {
        super(message);
    }
}