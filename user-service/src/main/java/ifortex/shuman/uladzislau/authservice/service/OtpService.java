package ifortex.shuman.uladzislau.authservice.service;


import ifortex.shuman.uladzislau.authservice.model.OtpType;

public interface OtpService {

  void generateAndSendOtp(String email, OtpType otpType);

  void validateOtp(String email, String otpCode, OtpType otpType);

  void generateAndSendOtpForEmailChange(Long userId, String newEmail);

  String validateAndRetrieveNewEmailFromOtp(Long userId, String otpCode);

  void resendOtp(String email, OtpType otpType);
}