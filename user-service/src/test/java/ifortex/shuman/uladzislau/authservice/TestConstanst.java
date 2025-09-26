package ifortex.shuman.uladzislau.authservice;

public final class TestConstanst {
    public static final String TEST_EMAIL = "test@example.com";
    public static final String TEST_VALID_PASSWORD = "ValidPassword1!";
    public static final String TEST_FULL_NAME = "Test User";
    public static final String TEST_PHONE = "+375293773738";
    public static final String TEST_GOOGLE_ID = "google-id-123";
    public static final String TEST_ACCESS_TOKEN = "access-token";
    public static final String TEST_REFRESH_TOKEN = "refresh-token";
    public static final Long TEST_USER_INDEX = 1L;
    public static final String TEST_NEW_EMAIL = "new.user@example.com";
    public static final String TEST_TEMP_PASSWORD = "tempPass123";
    public static final String TEST_UPDATE_USER_FULL_NAME = "Updated Name";
    public static final String TEST_RESET_TOKEN = "reset-token";
    public static final String TEST_INVALID_EMAIL = "not-an-email";
    public static final String TEST_INVALID_SHORT_PASSWORD = "short";
    public static final String TEST_OTP_CODE = "123456";
    public static final String TEST_NEW_ACCESS_TOKEN = "new-access-token";
    public static final String TEST_NEW_VALID_PASSWORD = "NewStrongPass1!";
    public static final String TEST_ADMIN_EMAIL = "newadmin@example.com";
    public static final String TEST_ADMIN_FULL_NAME = "New Admin";
    public static final String TEST_SUPER_ADMIN_EMAIL = "superadmin@example.com";
    public static final String TEST_HASHED_PASSWORD = "hashed_password";
    public static final String TEST_UUID_RESET_TOKEN = "uuid-reset-token";
    public static final String TEST_WRONG_PASSWORD = "WrongPassword123!";
    public static final String TEST_REDIS_KEY = "otp:test@example.com";
    public static final String TEST_REDIS_VALUE = "123456";
    public static final String TEST_WRONG_OTP_CODE = "wrong_code";
    public static final String TEST_ROLE = "ROLE_TESTER";
    public static final String TEST_SECRET_KEY =
            "NjQwREY2NTM1NzM2NTk2RTU4NzIzNjQ1NTk1QTYyNTY2NTY0NzM0RTU3NzUzNDU4NzEyMDQxM0Y0NDI4NDczMg==";
    public static final String TEST_OPAQUE_TOKEN = "test-opaque-token-12345";
    public static final String TEST_USER_TOKEN = "some-token";
    public static final String TEST_OTHER_EMAIL = "otheruser@example.com";

    public static final String LINKING_USER_ID_ATTRIBUTE = "linkingUserId";
    public static final String EMAIL_ATTRIBUTE = "email";
    public static final String TEST_FRONTEND_BASE_URL = "http://localhost:8080";
    public static final String TEST_RESET_PASSWORD_PATH = "/password/reset/form";
    public static final String TEST_RESET_URL = TEST_FRONTEND_BASE_URL + TEST_RESET_PASSWORD_PATH + "?token=" + TEST_UUID_RESET_TOKEN;

    public static final String API_PROFILE_ME = "/api/profile/me";
    public static final String API_AUTH_LOGIN = "/api/auth/login";
    public static final String API_AUTH_REGISTER = "/api/auth/register";
    public static final String API_ADMIN_USERS = "/api/admin/users";
    public static final String API_AUTH_PASSWORD = "/api/auth/password";
    public static final String API_PROFILE_2_FA = "/api/profile/2fa";
    public static final String API_PROFILE_CHANGE_PASSWORD = "/api/profile/change-password";
    public static final String API_PROFILE_LINK_GOOGLE = "/api/profile/link-google";
    public static final String API_PROFILE_REQUEST_EMAIL_CHANGE = "/api/profile/request-email-change";
    public static final String API_PROFILE_CONFIRM_EMAIL_CHANGE = "/api/profile/confirm-email-change";
    public static final String OAUTH_2_AUTHORIZATION_GOOGLE = "/oauth2/authorization/google";

    public static final String DELETE_TYPE_HARD_URL = "/hard";
    public static final String DELETE_TYPE_SOFT_URL = "/soft";
    public static final String BLOCK_URL = "/block";
    public static final String UNBLOCK_URL = "/unblock";
    public static final String RESET_PASSWORD_URL = "/reset-password";
    public static final String REQUEST_RESET_URL = "/request-reset";
    public static final String CONFIRM_RESET_URL = "/confirm-reset";
    public static final String CONFIRM_ADMIN_RESET_URL = "/confirm-admin-reset";

    public static final String ADMIN_ROLE = "ADMIN";
    public static final String SUPER_ADMIN_ROLE = "SUPER_ADMIN";
    public static final String CLIENT_ROLE = "CLIENT";

    public static final String RESET_USER_PASSWORD_MESSAGE = "Link sent";
    public static final String EMAIL_CHANGED_SUCCESSFULLY_MESSAGE = "Email changed successfully.";

    public static final String PHONE_WITHOUT_PLUS = "375291234567";
    public static final String UNPARSABLE_PHONE_NUMBER = "+invalid-number";
    public static final String INVALID_PHONE = "+1123";
    public static final String VALID_PHONE_NUMBER = "+375291234567";
}
