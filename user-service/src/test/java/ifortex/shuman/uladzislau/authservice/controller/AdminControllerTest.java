package ifortex.shuman.uladzislau.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ifortex.shuman.uladzislau.authservice.dto.UpdateUserByAdminRequestDto;
import ifortex.shuman.uladzislau.authservice.dto.UserDto;
import ifortex.shuman.uladzislau.authservice.model.Permissions;
import ifortex.shuman.uladzislau.authservice.model.User;
import ifortex.shuman.uladzislau.authservice.service.AdminUserService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static ifortex.shuman.uladzislau.authservice.TestConstanst.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private AdminUserService adminUserService;
  private User mockAdmin;

  @BeforeEach
  void setUp() {
    mockAdmin = new User();
    mockAdmin.setId(99L);
    mockAdmin.setEmail("admin@test.com");
  }


  @Test
  @WithMockUser(authorities = Permissions.ADMIN_USER_UNBLOCK)
  void unblockUser_BySuperAdmin_ShouldReturnOk() throws Exception {
    mockMvc.perform(post(API_ADMIN_USERS + "/" + TEST_USER_INDEX + UNBLOCK_URL))
        .andExpect(status().isOk());
    //verify(adminUserService).unblockUser(1L);
  }

  @Test
  @WithMockUser(authorities = Permissions.ADMIN_USER_READ)
  void unblockUser_ByAdmin_ShouldReturnForbidden() throws Exception {
    mockMvc.perform(post(API_ADMIN_USERS + "/" + TEST_USER_INDEX + UNBLOCK_URL))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(authorities = Permissions.ADMIN_USER_DELETE_SOFT)
  void softDeleteUser_ByAdmin_ShouldReturnNoContent() throws Exception {
    mockMvc.perform(delete(API_ADMIN_USERS + "/" + TEST_USER_INDEX + DELETE_TYPE_SOFT_URL))
        .andExpect(status().isNoContent());
    verify(adminUserService).softDeleteUser(1L, new User());
  }

  @Test
  @WithMockUser(authorities = Permissions.ADMIN_USER_DELETE_HARD)
  void hardDeleteUser_BySuperAdmin_ShouldReturnNoContent() throws Exception {
    mockMvc.perform(delete(API_ADMIN_USERS + "/" + TEST_USER_INDEX + DELETE_TYPE_HARD_URL))
        .andExpect(status().isNoContent());
    verify(adminUserService).hardDeleteUser(1L, new User());
  }

  @Test
  @WithMockUser(authorities = Permissions.ADMIN_USER_READ)
  void findUsersByCriteria_ShouldReturnPageOfUsers() throws Exception {
    Page<UserDto> userPage = new PageImpl<>(
        Collections.singletonList(UserDto.builder().id(1L).build()));
    when(adminUserService.findUsersByComplexFilter(any(), any(Pageable.class)))
        .thenReturn(userPage);

    mockMvc.perform(get(API_ADMIN_USERS)
            .param("page", "0").param("size", "10")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content[0].id").value(1L));
  }

  @Test
  @WithMockUser(authorities = Permissions.ADMIN_USER_UPDATE)
  void updateUser_ByAdmin_ShouldReturnOkWithUpdatedUser() throws Exception {
    UpdateUserByAdminRequestDto requestDto = new UpdateUserByAdminRequestDto();
    requestDto.setFullName(TEST_UPDATE_USER_FULL_NAME);
    UserDto updatedUserDto = UserDto.builder().id(1L).fullName(TEST_UPDATE_USER_FULL_NAME).build();
    when(adminUserService.updateUserByAdmin(eq(1L), any(UpdateUserByAdminRequestDto.class)))
        .thenReturn(updatedUserDto);

    mockMvc.perform(
            put(API_ADMIN_USERS + "/" + TEST_USER_INDEX)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.fullName").value(TEST_UPDATE_USER_FULL_NAME));
  }

  @Test
  @WithMockUser(authorities = "some_other_permission")
  void blockUserTemporarily_ByUserWithoutPermission_ShouldReturnForbidden() throws Exception {
    mockMvc.perform(post(API_ADMIN_USERS + "/" + TEST_USER_INDEX + "/block-temporarily")
            .param("duration", "24"))
        .andExpect(status().isForbidden());
  }
}