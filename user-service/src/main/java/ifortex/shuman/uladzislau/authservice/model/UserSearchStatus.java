package ifortex.shuman.uladzislau.authservice.model;

public enum UserSearchStatus {
    ACTIVE,
    DELETED,
    PASSWORD_RESET_PENDING,
    PENDING_VERIFICATION,
    PENDING_DELETION,
    BLOCKED
}