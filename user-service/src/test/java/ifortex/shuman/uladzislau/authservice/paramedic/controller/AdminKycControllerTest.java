package ifortex.shuman.uladzislau.authservice.paramedic.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ifortex.shuman.uladzislau.authservice.AuthserviceApplication;
import ifortex.shuman.uladzislau.authservice.model.Permissions;
import ifortex.shuman.uladzislau.authservice.model.User;
import ifortex.shuman.uladzislau.authservice.paramedic.dto.KycApplicationDetailsDto;
import ifortex.shuman.uladzislau.authservice.paramedic.dto.KycApplicationSummaryDto;
import ifortex.shuman.uladzislau.authservice.paramedic.dto.KycRejectionRequestDto;
import ifortex.shuman.uladzislau.authservice.paramedic.service.KycService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = AuthserviceApplication.class)
@AutoConfigureMockMvc
class AdminKycControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private KycService kycService;

  private User adminUser;
  private UsernamePasswordAuthenticationToken adminAuth;

  @BeforeEach
  void setUp() {
    adminUser = new User();
    adminUser.setId(99L);
    adminUser.setEmail("admin@test.com");

    List<SimpleGrantedAuthority> authorities = List.of(
        new SimpleGrantedAuthority(Permissions.ADMIN_KYC_READ),
        new SimpleGrantedAuthority(Permissions.ADMIN_KYC_MANAGE)
    );
    adminAuth = new UsernamePasswordAuthenticationToken(adminUser, null, authorities);
  }

  @Test
  void getPendingApplications_whenAuthorized_shouldReturnPageOfApplications() throws Exception {
    Page<KycApplicationDetailsDto> page = new PageImpl<>(Collections.emptyList());
    when(kycService.getPendingApplications(any(PageRequest.class))).thenReturn(page);

    mockMvc.perform(get("/api/admin/kyc/pending")
            .with(authentication(adminAuth))
            .header("X-Internal-Gateway-Call", "true")
            .param("page", "0")
            .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray());
  }

  @Test
  void getApplicationDetails_whenAuthorized_shouldReturnApplicationDetails() throws Exception {
    long applicationId = 101L;
    KycApplicationDetailsDto detailsDto = KycApplicationDetailsDto.builder()
        .applicationId(applicationId)
        .build();
    when(kycService.getApplicationDetails(applicationId)).thenReturn(detailsDto);

    mockMvc.perform(get("/api/admin/kyc/applications/{applicationId}", applicationId)
            .with(authentication(adminAuth))
            .header("X-Internal-Gateway-Call", "true"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.applicationId").value(applicationId));
  }
}