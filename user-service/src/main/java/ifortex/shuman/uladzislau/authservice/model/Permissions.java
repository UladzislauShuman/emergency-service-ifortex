package ifortex.shuman.uladzislau.authservice.model;

public class Permissions {
    public static final String ADMIN_USER_READ = "admin:user:read";
    public static final String  ADMIN_USER_CREATE = "admin:user:create";
    public static final String ADMIN_CREATE_CLIENT = "admin:create:client";
    public static final String ADMIN_CREATE_PARAMEDIC = "admin:create:paramedic";
    public static final String ADMIN_CREATE_ADMIN = "admin:create:admin";
    public static final String ADMIN_CREATE_SUPER_ADMIN = "admin:create:super_admin";
    public static final String  ADMIN_USER_UPDATE = "admin:user:update";
    public static final String  ADMIN_USER_DELETE_SOFT = "admin:user:delete:soft";
    public static final String  ADMIN_USER_DELETE_HARD = "admin:user:delete:hard";
    public static final String  ADMIN_USER_BLOCK = "admin:user:block";
    public static final String  ADMIN_USER_UNBLOCK = "admin:user:unblock";
    public static final String  ADMIN_USER_RESET_PASSWORD = "admin:user:reset-password";
    public static final String ADMIN_KYC_READ = "admin:kyc:read";
    public static final String ADMIN_KYC_MANAGE = "admin:kyc:manage";

    public static final String ADMIN_USER_RESET_PASSWORD_RESET_LINK = "admin:user:reset-password:reset-link";
    public static final String ADMIN_USER_RESET_PASSWORD_GENERATE_TEMP = "admin:user:reset-password:generate-temp";

    public static final String PARAMEDIC_APPLICATION_READ = "paramedic:application:read";
    public static final String PARAMEDIC_EMERGENCY_ACCEPT = "paramedic:emergency:accept";

    public final static String SUBSCRIPTION_CREATE = "subscription:create";
    public final static String SUBSCRIPTION_MANAGE = "subscription:manage";
}