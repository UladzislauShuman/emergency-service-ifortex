package ifortex.shuman.uladzislau.authservice.paramedic.controller.frontend;

import ifortex.shuman.uladzislau.authservice.AuthserviceApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest(classes = AuthserviceApplication.class)
@AutoConfigureMockMvc
class AdminKycPageControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  @WithAnonymousUser
  void kycDashboardPage_shouldReturnDashboardView() throws Exception {
    mockMvc.perform(get("/admin/kyc-dashboard"))
        .andExpect(status().isOk())
        .andExpect(view().name("admin-kyc-dashboard"));
  }

  @Test
  @WithAnonymousUser
  void kycDetailsPage_shouldReturnDetailsView() throws Exception {
    mockMvc.perform(get("/admin/kyc-details"))
        .andExpect(status().isOk())
        .andExpect(view().name("admin-kyc-details"));
  }
}