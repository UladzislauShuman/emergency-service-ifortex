package ifortex.shuman.uladzislau.authservice.controller.frontend;

import ifortex.shuman.uladzislau.authservice.AuthserviceApplication;
import ifortex.shuman.uladzislau.authservice.dto.HomePageDetailsDto;
import ifortex.shuman.uladzislau.authservice.dto.UserDto;
import ifortex.shuman.uladzislau.authservice.model.User;
import ifortex.shuman.uladzislau.authservice.service.HomePageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = AuthserviceApplication.class)
@AutoConfigureMockMvc
class HomePageControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private HomePageService homePageService;

  private User currentUser;

  @BeforeEach
  void setUp() {
    currentUser = new User();
    currentUser.setId(1L);
    currentUser.setEmail("test@example.com");
  }

  @Test
  void getHomePageDetails_whenAuthenticated_shouldReturnDetails() throws Exception {
    HomePageDetailsDto detailsDto = HomePageDetailsDto.builder()
        .user(UserDto.builder().email("test@example.com").build())
        .permissions(Collections.emptySet())
        .build();

    when(homePageService.getHomePageDetails(currentUser)).thenReturn(detailsDto);

    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
        currentUser, null, Collections.emptyList());

    mockMvc.perform(get("/api/home/details")
            .with(authentication(authToken))
            .header("X-Internal-Gateway-Call", "true"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.user.email").value("test@example.com"));
  }
}