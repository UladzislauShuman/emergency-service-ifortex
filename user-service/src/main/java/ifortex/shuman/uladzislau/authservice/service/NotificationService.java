package ifortex.shuman.uladzislau.authservice.service;

import ifortex.shuman.uladzislau.authservice.model.OtpType;

public interface NotificationService {

  void sendOtp(String toEmail, String otp, OtpType otpType);

  void sendPasswordResetEmail(String toEmail, String resetUrl);

  void sendTemporaryPasswordEmail(String toEmail, String temporaryPassword);

  void sendEmailChangeNotificationToOldEmail(String oldEmail, String newEmail);

  void sendKycApprovalEmail(String toEmail, String userName);

  void sendKycRejectionEmail(String toEmail, String userName, String reason);

  void sendParamedicApprovalNotification(String applicantEmail, String userName, String workEmail, String workEmailPassword);
}