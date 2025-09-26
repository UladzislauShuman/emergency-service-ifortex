package ifortex.shuman.uladzislau.authservice.service;

import ifortex.shuman.uladzislau.authservice.dto.AdminPasswordResetResponseDto;
import ifortex.shuman.uladzislau.authservice.dto.BlockUserRequestDto;
import ifortex.shuman.uladzislau.authservice.dto.MessageResponseDto;
import ifortex.shuman.uladzislau.authservice.dto.UpdateUserByAdminRequestDto;
import ifortex.shuman.uladzislau.authservice.dto.UserDto;
import ifortex.shuman.uladzislau.authservice.dto.UserSearchRequestDto;
import ifortex.shuman.uladzislau.authservice.model.User;
import ifortex.shuman.uladzislau.authservice.model.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public interface AdminUserService {

  UserDto updateUserByAdmin(Long userId, UpdateUserByAdminRequestDto request);

  UserDto updateBlockStatus(User whoBlocking, Long userToBlockId, BlockUserRequestDto request);

  MessageResponseDto softDeleteUser(Long userId, User currentUser);

  MessageResponseDto hardDeleteUser(Long userId, User currentUser);

  Page<UserDto> findUsersByComplexFilter(
      UserSearchRequestDto request, Pageable pageable);

  AdminPasswordResetResponseDto sendPasswordResetLink(Long userId);

  AdminPasswordResetResponseDto generateTemporaryPassword(Long userId);

  UserDto cancelAccountDeletion(Long userId);
}
