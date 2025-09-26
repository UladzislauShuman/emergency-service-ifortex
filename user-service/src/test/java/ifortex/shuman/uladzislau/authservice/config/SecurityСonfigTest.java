package ifortex.shuman.uladzislau.authservice.config;

import ifortex.shuman.uladzislau.authservice.model.Permissions;
import ifortex.shuman.uladzislau.authservice.model.User;
import ifortex.shuman.uladzislau.authservice.service.implemention.AdminUserServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static ifortex.shuman.uladzislau.authservice.TestConstanst.ADMIN_ROLE;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.API_ADMIN_USERS;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.API_AUTH_LOGIN;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.API_AUTH_REGISTER;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.API_PROFILE_ME;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.CLIENT_ROLE;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.DELETE_TYPE_HARD_URL;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_USER_INDEX;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {



  @MockBean
  private AdminUserServiceImpl adminUserService;

  @Autowired
  private MockMvc mockMvc;


  @Test
  void publicEndpoints_WhenNotAuthenticated_ShouldBeAccessible() throws Exception {
    mockMvc.perform(post(API_AUTH_LOGIN)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest());

    mockMvc.perform(post(API_AUTH_REGISTER)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void protectedEndpoints_WhenNotAuthenticated_ShouldReturnForbidden() throws Exception {
    mockMvc.perform(get(API_PROFILE_ME))
        .andExpect(status().isForbidden());

    mockMvc.perform(get(API_ADMIN_USERS))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = CLIENT_ROLE)
  void adminEndpoint_WhenAccessedByClient_ShouldReturnForbidden() throws Exception {
    mockMvc.perform(get(API_ADMIN_USERS))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(authorities = Permissions.ADMIN_USER_READ)
  void adminEndpoint_WhenAccessedByAdmin_ShouldReturnOk() throws Exception {
    mockMvc.perform(get(API_ADMIN_USERS))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(authorities = Permissions.ADMIN_USER_DELETE_HARD)
  void hardDeleteEndpoint_WhenAccessedBySuperAdmin_ShouldBeAllowed() throws Exception {
    doNothing().when(adminUserService).hardDeleteUser(anyLong(), any(User.class));

    mockMvc.perform(delete(API_ADMIN_USERS + "/" + TEST_USER_INDEX + DELETE_TYPE_HARD_URL))
        .andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser(roles = ADMIN_ROLE)
  void hardDeleteEndpoint_WhenAccessedByAdmin_ShouldReturnForbidden() throws Exception {
    mockMvc.perform(delete(API_ADMIN_USERS + "/" + TEST_USER_INDEX + DELETE_TYPE_HARD_URL))
        .andExpect(status().isForbidden());
  }
}