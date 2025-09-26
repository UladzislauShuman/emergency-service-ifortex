package ifortex.shuman.uladzislau.authservice.controller.frontend;

import ifortex.shuman.uladzislau.authservice.TestConstanst;
import ifortex.shuman.uladzislau.authservice.config.RestAuthenticationEntryPoint;
import ifortex.shuman.uladzislau.authservice.config.SecurityConfig;
import ifortex.shuman.uladzislau.authservice.service.JwtService;
import ifortex.shuman.uladzislau.authservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(AuthPageController.class)
@Import(SecurityConfig.class)
class AuthPageControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private JwtService jwtService;
  @MockBean
  private UserDetailsService userDetailsService;
  @MockBean
  private AuthenticationProvider authenticationProvider;
  @MockBean
  private UserService userService;
  @MockBean
  private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

  @Test
  @WithAnonymousUser
  void homePage_shouldReturnHomePage() throws Exception {
    mockMvc.perform(get("/home"))
        .andExpect(status().isOk())
        .andExpect(view().name("home"));
  }

  @Test
  @WithAnonymousUser
  void loginPage_shouldReturnLoginPage() throws Exception {
    mockMvc.perform(get("/login"))
        .andExpect(status().isOk())
        .andExpect(view().name("login"));
  }

  @Test
  @WithAnonymousUser
  void registerPage_shouldReturnRegisterPage() throws Exception {
    mockMvc.perform(get("/register"))
        .andExpect(status().isOk())
        .andExpect(view().name("register"));
  }

  @Test
  @WithAnonymousUser
  void verifyEmailPage_shouldReturnVerifyEmailPageWithEmailAttribute() throws Exception {
    mockMvc.perform(get("/verify-email").param("email", TestConstanst.TEST_EMAIL))
        .andExpect(status().isOk())
        .andExpect(view().name("verify-email"))
        .andExpect(model().attribute("email", TestConstanst.TEST_EMAIL));
  }

  @Test
  @WithAnonymousUser
  void verify2faPage_shouldReturnVerify2faPageWithEmailAttribute() throws Exception {
    mockMvc.perform(get("/verify-2fa").param("email", TestConstanst.TEST_EMAIL))
        .andExpect(status().isOk())
        .andExpect(view().name("verify-2fa"))
        .andExpect(model().attribute("email", TestConstanst.TEST_EMAIL));
  }

  @Test
  @WithAnonymousUser
  void rootPage_shouldReturnIndexPage() throws Exception {
    mockMvc.perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(view().name("index"));
  }
}