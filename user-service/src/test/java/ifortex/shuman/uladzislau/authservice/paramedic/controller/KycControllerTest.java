package ifortex.shuman.uladzislau.authservice.paramedic.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ifortex.shuman.uladzislau.authservice.AuthserviceApplication;
import ifortex.shuman.uladzislau.authservice.model.User;
import ifortex.shuman.uladzislau.authservice.paramedic.dto.KycStatusResponseDto;
import ifortex.shuman.uladzislau.authservice.paramedic.dto.KycSubmissionRequestDto;
import ifortex.shuman.uladzislau.authservice.paramedic.model.ParamedicApplicationStatus;
import ifortex.shuman.uladzislau.authservice.paramedic.service.KycService;
import ifortex.shuman.uladzislau.authservice.service.UserService;
import ifortex.shuman.uladzislau.authservice.service.permission.PermissionService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import org.springframework.web.multipart.MultipartFile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = AuthserviceApplication.class)
@AutoConfigureMockMvc
class KycControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private KycService kycService;
  @MockBean
  private UserService userService;
  @MockBean
  private UserDetailsService userDetailsService;
  @MockBean
  private PermissionService permissionService;

  private User currentUser;
  private final String OPAQUE_TOKEN = "test-opaque-token-kyc";

  @BeforeEach
  void setUp() {
    currentUser = new User();
    currentUser.setId(1L);
    currentUser.setEmail("paramedic@test.com");

    when(userService.findById(currentUser.getId())).thenReturn(currentUser);
    when(userDetailsService.loadUserByUsername(currentUser.getEmail())).thenReturn(currentUser);
  }
}