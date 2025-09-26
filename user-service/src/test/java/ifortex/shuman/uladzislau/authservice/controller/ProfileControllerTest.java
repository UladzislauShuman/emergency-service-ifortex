package ifortex.shuman.uladzislau.authservice.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import ifortex.shuman.uladzislau.authservice.WithMockCustomUser;
import ifortex.shuman.uladzislau.authservice.dto.ChangePasswordRequestDto;
import ifortex.shuman.uladzislau.authservice.dto.EmailChangeConfirmDto;
import ifortex.shuman.uladzislau.authservice.dto.EmailChangeRequestDto;
import ifortex.shuman.uladzislau.authservice.dto.EmailChangeResponseDto;
import ifortex.shuman.uladzislau.authservice.dto.UpdateProfileRequestDto;
import ifortex.shuman.uladzislau.authservice.dto.UserDto;
import ifortex.shuman.uladzislau.authservice.model.User;
import ifortex.shuman.uladzislau.authservice.service.ProfileUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static ifortex.shuman.uladzislau.authservice.TestConstanst.API_PROFILE_2_FA;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.API_PROFILE_CHANGE_PASSWORD;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.API_PROFILE_CONFIRM_EMAIL_CHANGE;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.API_PROFILE_LINK_GOOGLE;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.API_PROFILE_ME;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.API_PROFILE_REQUEST_EMAIL_CHANGE;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.EMAIL_CHANGED_SUCCESSFULLY_MESSAGE;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.LINKING_USER_ID_ATTRIBUTE;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.OAUTH_2_AUTHORIZATION_GOOGLE;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_EMAIL;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_NEW_EMAIL;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_NEW_VALID_PASSWORD;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_OTP_CODE;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_VALID_PASSWORD;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.mockito.ArgumentMatchers.argThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProfileControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private ProfileUserService profileUserService;

  @Test
  @WithMockCustomUser
  void getCurrentUser_ShouldReturnCurrentUserProfile() throws Exception {
    UserDto userDto = UserDto.builder().id(1L).email(TEST_EMAIL).build();
    when(profileUserService.getUserProfile(any(Long.class))).thenReturn(userDto);

    mockMvc.perform(get(API_PROFILE_ME))

        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.email").value(TEST_EMAIL));
  }

  @Test
  @WithMockCustomUser
  void updateProfile_WithValidData_ShouldReturnOk() throws Exception {
    UpdateProfileRequestDto requestDto = new UpdateProfileRequestDto();
    requestDto.setFullName("New Full Name");

    mockMvc.perform(put(API_PROFILE_ME)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestDto)))
        .andExpect(status().isOk());

    verify(profileUserService).updateProfile(any(Long.class), any(UpdateProfileRequestDto.class));
  }

  @Test
  @WithMockCustomUser(id = 5L)
  void set2FA_ShouldCallServiceWithCorrectParameters() throws Exception {
    mockMvc.perform(post(API_PROFILE_2_FA)
            .param("enable", "true"))
        .andExpect(status().isOk());

    verify(profileUserService).setTwoFactorAuthentication(
        argThat(user -> user.getId() == 5L),
        eq(true)
    );
  }

  @Test
  void getCurrentUser_WhenNotAuthenticated_ShouldReturnUnauthorized() throws Exception {
    mockMvc.perform(get(API_PROFILE_ME))
        .andExpect(status().isUnauthorized());
  }


  @Test
  @WithMockCustomUser
  void changePassword_WithValidData_ShouldReturnOk() throws Exception {
    ChangePasswordRequestDto requestDto = new ChangePasswordRequestDto();
    requestDto.setCurrentPassword(TEST_VALID_PASSWORD);
    requestDto.setNewPassword(TEST_NEW_VALID_PASSWORD);
    requestDto.setConfirmationPassword(TEST_NEW_VALID_PASSWORD);

    mockMvc.perform(
            post(API_PROFILE_CHANGE_PASSWORD)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
        .andExpect(status().isOk());

    verify(profileUserService).changePassword(any(User.class), any(ChangePasswordRequestDto.class));
  }

  @Test
  @WithMockCustomUser(id = 123L)
  void linkGoogleAccount_ShouldSetSessionAttributeAndRedirect() throws Exception {
    MvcResult result = mockMvc.perform(
            get(API_PROFILE_LINK_GOOGLE))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(OAUTH_2_AUTHORIZATION_GOOGLE))
        .andReturn();

    MockHttpSession session = (MockHttpSession) result.getRequest().getSession();
    assertThat(session.getAttribute(LINKING_USER_ID_ATTRIBUTE)).isEqualTo(123L);
  }

  @Test
  @WithMockCustomUser
  void requestEmailChange_WithValidData_ShouldReturnOk() throws Exception {
    EmailChangeRequestDto requestDto = new EmailChangeRequestDto();
    requestDto.setCurrentPassword(TEST_VALID_PASSWORD);
    requestDto.setNewEmail(TEST_NEW_EMAIL);

    mockMvc.perform(
            post(API_PROFILE_REQUEST_EMAIL_CHANGE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
        .andExpect(status().isOk());

    verify(profileUserService).requestEmailChange(any(User.class),
        any(EmailChangeRequestDto.class));
  }

  @Test
  @WithMockCustomUser
  void confirmEmailChange_WithValidData_ShouldReturnOkAndResponse() throws Exception {
    EmailChangeConfirmDto requestDto = new EmailChangeConfirmDto();
    requestDto.setOtpCode(TEST_OTP_CODE);

    EmailChangeResponseDto responseDto = EmailChangeResponseDto.builder()
        .message(EMAIL_CHANGED_SUCCESSFULLY_MESSAGE)
        .reLoginRequired(true)
        .build();
    when(profileUserService.confirmEmailChange(any(User.class), any(EmailChangeConfirmDto.class)))
        .thenReturn(responseDto);

    mockMvc.perform(
            post(API_PROFILE_CONFIRM_EMAIL_CHANGE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.reLoginRequired").value(true))
        .andExpect(jsonPath("$.message").value(EMAIL_CHANGED_SUCCESSFULLY_MESSAGE));
  }
}