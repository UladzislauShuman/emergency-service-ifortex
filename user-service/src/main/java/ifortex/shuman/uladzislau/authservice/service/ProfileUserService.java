package ifortex.shuman.uladzislau.authservice.service;

import ifortex.shuman.uladzislau.authservice.dto.ChangePasswordRequestDto;
import ifortex.shuman.uladzislau.authservice.dto.EmailChangeConfirmDto;
import ifortex.shuman.uladzislau.authservice.dto.EmailChangeRequestDto;
import ifortex.shuman.uladzislau.authservice.dto.EmailChangeResponseDto;
import ifortex.shuman.uladzislau.authservice.dto.MessageResponseDto;
import ifortex.shuman.uladzislau.authservice.dto.UpdateProfileRequestDto;
import ifortex.shuman.uladzislau.authservice.dto.UserDto;
import ifortex.shuman.uladzislau.authservice.model.User;

public interface ProfileUserService {

  UserDto updateProfile(Long userId, UpdateProfileRequestDto request);

  MessageResponseDto changePassword(User user, ChangePasswordRequestDto request);

  MessageResponseDto setTwoFactorAuthentication(User user, boolean enable);

  MessageResponseDto requestEmailChange(User user, EmailChangeRequestDto request);

  EmailChangeResponseDto confirmEmailChange(User user, EmailChangeConfirmDto request);

  MessageResponseDto linkGoogleAccount(Long userId, String googleId, String googleEmail);

  MessageResponseDto requestAccountDeletion(Long userId);

  UserDto getUserProfile(Long userId);
}