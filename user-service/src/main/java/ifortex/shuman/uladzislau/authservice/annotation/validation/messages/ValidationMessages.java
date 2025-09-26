package ifortex.shuman.uladzislau.authservice.annotation.validation.messages;

public final class ValidationMessages {

    private ValidationMessages() {}

    public static final String EMAIL_NOT_BLANK = "Email cannot be blank";
    public static final String EMAIL_INVALID_FORMAT = "Email should be valid";
    public static final String EMAIL_MAX_SIZE = "Email must be less than 100 characters";

    public static final String FULL_NAME_NOT_BLANK = "Full name cannot be blank";
    public static final String FULL_NAME_MAX_SIZE = "Full name must be less than 100 characters";

    public static final String PHONE_NOT_BLANK = "Phone number cannot be blank";

    public static final String PASSWORD_NOT_BLANK = "Password cannot be blank";
    public static final String PASSWORD_CONFIRMATION_NOT_BLANK = "Password confirmation cannot be blank";
    public static final String PASSWORD_MAX_SIZE = "Password must be between 8 and 100 characters";
    public static final String PASSWORD_RULES = "Password must contain at least one digit, one lowercase letter, " +
            "one uppercase letter, and one special character";
    public static final String PASSWORD_INVALID = "Invalid password";

    public static final String ROLE_NOT_NULL = "Role cannot be null";

    public static final String REFRESH_TOKEN_NOT_BLANK = "Refresh token cannot be blank";

    public static final String PHONE_INVALID_FORMAT = "Invalid phone number format." +
            "The number must be in international E.164 format";

    public static final String OTP_NOT_BLANK = "OTP code cannot be blank";
}