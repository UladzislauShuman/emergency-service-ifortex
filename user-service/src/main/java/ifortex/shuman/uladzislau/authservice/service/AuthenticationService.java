package ifortex.shuman.uladzislau.authservice.service;

import ifortex.shuman.uladzislau.authservice.dto.AdminPasswordResetConfirmDto;
import ifortex.shuman.uladzislau.authservice.dto.JwtTokenDto;
import ifortex.shuman.uladzislau.authservice.dto.LoginRequestDto;
import ifortex.shuman.uladzislau.authservice.dto.LoginResponseDto;
import ifortex.shuman.uladzislau.authservice.dto.MessageResponseDto;
import ifortex.shuman.uladzislau.authservice.dto.PasswordResetConfirmDto;
import ifortex.shuman.uladzislau.authservice.dto.RefreshTokenRequestDto;
import ifortex.shuman.uladzislau.authservice.dto.RegisterRequestDto;
import ifortex.shuman.uladzislau.authservice.dto.ResendOtpRequestDto;
import ifortex.shuman.uladzislau.authservice.dto.VerificationRequestDto;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthenticationService {

  MessageResponseDto register(RegisterRequestDto request);

  MessageResponseDto verifyEmail(VerificationRequestDto request);

  LoginResponseDto login(LoginRequestDto request);

  LoginResponseDto verify2FA(VerificationRequestDto request);

  JwtTokenDto refreshAccessToken(RefreshTokenRequestDto request);

  MessageResponseDto requestPasswordReset(String email);

  MessageResponseDto confirmPasswordReset(PasswordResetConfirmDto request);

  MessageResponseDto confirmAdminPasswordReset(AdminPasswordResetConfirmDto request);

  MessageResponseDto resendOtp(ResendOtpRequestDto request);

  MessageResponseDto logout(HttpServletRequest request);
}
