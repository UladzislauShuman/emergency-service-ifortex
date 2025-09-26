package ifortex.shuman.uladzislau.authservice.service;

import ifortex.shuman.uladzislau.authservice.dto.CreateUserByAdminRequestDto;
import ifortex.shuman.uladzislau.authservice.dto.CreateUserResponseDto;
import ifortex.shuman.uladzislau.authservice.model.UserRole;

public interface UserCreationService {

  CreateUserResponseDto createUser(CreateUserByAdminRequestDto request, UserRole role);
}