package ifortex.shuman.uladzislau.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ifortex.shuman.uladzislau.authservice.TestConstanst;
import ifortex.shuman.uladzislau.authservice.dto.AdminPasswordResetConfirmDto;
import ifortex.shuman.uladzislau.authservice.dto.JwtTokenDto;
import ifortex.shuman.uladzislau.authservice.dto.LoginRequestDto;
import ifortex.shuman.uladzislau.authservice.dto.LoginResponseDto;
import ifortex.shuman.uladzislau.authservice.dto.PasswordResetConfirmDto;
import ifortex.shuman.uladzislau.authservice.dto.PasswordResetRequestDto;
import ifortex.shuman.uladzislau.authservice.dto.RefreshTokenRequestDto;
import ifortex.shuman.uladzislau.authservice.dto.RegisterRequestDto;
import ifortex.shuman.uladzislau.authservice.dto.VerificationRequestDto;
import ifortex.shuman.uladzislau.authservice.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static ifortex.shuman.uladzislau.authservice.TestConstanst.API_AUTH_LOGIN;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.API_AUTH_PASSWORD;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.API_AUTH_REGISTER;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.CONFIRM_ADMIN_RESET_URL;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.CONFIRM_RESET_URL;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.REQUEST_RESET_URL;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_ACCESS_TOKEN;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_EMAIL;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_FULL_NAME;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_INVALID_EMAIL;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_INVALID_SHORT_PASSWORD;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_NEW_ACCESS_TOKEN;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_NEW_VALID_PASSWORD;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_OPAQUE_TOKEN;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_OTP_CODE;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_PHONE;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_REFRESH_TOKEN;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_UUID_RESET_TOKEN;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_VALID_PASSWORD;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationService authenticationService;

    @Test
    void register_WithValidRequest_ShouldReturnOkAndTriggerOtp() throws Exception {
        RegisterRequestDto request = new RegisterRequestDto();
        request.setEmail(TestConstanst.TEST_EMAIL);
        request.setFullName(TEST_FULL_NAME);
        request.setPhone(TEST_PHONE);
        request.setPassword(TEST_VALID_PASSWORD);
        request.setPasswordConfirmation(TEST_VALID_PASSWORD);

        doNothing().when(authenticationService).register(any(RegisterRequestDto.class));

        mockMvc.perform(post(API_AUTH_REGISTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(authenticationService).register(any(RegisterRequestDto.class));
    }

    @Test
    void verifyEmail_WithValidOtp_ShouldReturnOkAndTokens() throws Exception {
        VerificationRequestDto request = new VerificationRequestDto();
        request.setEmail(TEST_EMAIL);
        request.setOtpCode(TEST_OTP_CODE);
        LoginResponseDto responseDto = LoginResponseDto.builder()
            .build();

        //hen(authenticationService.verifyEmail(any(VerificationRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/auth/verify-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.opaqueToken").value(TEST_OPAQUE_TOKEN))
            .andExpect(jsonPath("$.accessToken").doesNotExist())
            .andExpect(jsonPath("$.refreshToken").doesNotExist());
    }

    @Test
    void verifyEmail_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        VerificationRequestDto request = new VerificationRequestDto();
        request.setEmail("not-an-email");
        request.setOtpCode("");

        mockMvc.perform(post("/api/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_WithInvalidEmail_ShouldReturnBadRequest() throws Exception {
        RegisterRequestDto request = new RegisterRequestDto();
        request.setEmail(TEST_INVALID_EMAIL);
        request.setFullName(TEST_FULL_NAME);
        request.setPassword(TEST_VALID_PASSWORD);
        request.setPasswordConfirmation(TEST_VALID_PASSWORD);

        mockMvc.perform(post(API_AUTH_REGISTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_WithShortPassword_ShouldReturnBadRequest() throws Exception {
        RegisterRequestDto request = new RegisterRequestDto();
        request.setEmail(TestConstanst.TEST_EMAIL);
        request.setFullName(TEST_FULL_NAME);
        request.setPassword(TEST_INVALID_SHORT_PASSWORD);
        request.setPasswordConfirmation(TEST_INVALID_SHORT_PASSWORD);

        mockMvc.perform(post(API_AUTH_REGISTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

//    @Test
//    void login_WithValidCredentials_ShouldReturnLoginResponse() throws Exception {
//        LoginRequestDto request = new LoginRequestDto();
//        request.setEmail(TestConstanst.TEST_EMAIL);
//        request.setPassword(TEST_VALID_PASSWORD);
//
//        LoginResponseDto response = LoginResponseDto.builder()
//                .accessToken(TEST_ACCESS_TOKEN)
//                .refreshToken(TEST_REFRESH_TOKEN)
//                .twoFARequired(false)
//                .build();
//        when(authenticationService.login(any(LoginRequestDto.class))).thenReturn(response);
//
//        mockMvc.perform(post(API_AUTH_LOGIN)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.accessToken").value(TEST_ACCESS_TOKEN))
//                .andExpect(jsonPath("$.twoFARequired").value(false));
//    }

//    @Test
//    void verify2FA_WithValidCode_ShouldReturnTokens() throws Exception {
//        VerificationRequestDto request = new VerificationRequestDto();
//        request.setEmail(TestConstanst.TEST_EMAIL);
//        request.setOtpCode(TEST_OTP_CODE);
//
//        JwtTokenDto response = new JwtTokenDto(TEST_ACCESS_TOKEN, TEST_REFRESH_TOKEN);
//        when(authenticationService.verify2FA(any(VerificationRequestDto.class))).thenReturn(response);
//
//        mockMvc.perform(post("/api/auth/verify-2fa")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.accessToken").value(TEST_ACCESS_TOKEN));
//    }

    @Test
    void refreshToken_WithValidToken_ShouldReturnNewTokens() throws Exception {
        RefreshTokenRequestDto request = new RefreshTokenRequestDto();
        request.setRefreshToken(TEST_REFRESH_TOKEN);

        JwtTokenDto response = new JwtTokenDto(TEST_NEW_ACCESS_TOKEN, TEST_UUID_RESET_TOKEN);
        when(authenticationService.refreshAccessToken(any(RefreshTokenRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(TEST_NEW_ACCESS_TOKEN));
    }

    @Test
    void requestPasswordReset_WithValidEmail_ShouldReturnOk() throws Exception {
        PasswordResetRequestDto request = new PasswordResetRequestDto();
        request.setEmail(TEST_EMAIL);

        mockMvc.perform(post(API_AUTH_PASSWORD + REQUEST_RESET_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(authenticationService).requestPasswordReset(TEST_EMAIL);
    }

    @Test
    void confirmAdminPasswordReset_WithValidData_ShouldReturnOk() throws Exception {
        AdminPasswordResetConfirmDto request = new AdminPasswordResetConfirmDto();
        request.setToken(TEST_UUID_RESET_TOKEN);
        request.setNewPassword(TEST_NEW_VALID_PASSWORD);
        request.setConfirmationPassword(TEST_NEW_VALID_PASSWORD);

        mockMvc.perform(post(API_AUTH_PASSWORD + CONFIRM_ADMIN_RESET_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(authenticationService).confirmAdminPasswordReset(any(AdminPasswordResetConfirmDto.class));
    }

    @Test
    void confirmPasswordReset_WithValidOtpAndPasswords_ShouldReturnOk() throws Exception {
        PasswordResetConfirmDto request = new PasswordResetConfirmDto();
        request.setEmail(TEST_EMAIL);
        request.setOtpCode(TEST_OTP_CODE);
        request.setNewPassword(TEST_NEW_VALID_PASSWORD);
        request.setConfirmationPassword(TEST_NEW_VALID_PASSWORD);

        mockMvc.perform(post(API_AUTH_PASSWORD + CONFIRM_RESET_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(authenticationService).confirmPasswordReset(any(PasswordResetConfirmDto.class));
    }

    @Test
    void confirmPasswordReset_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        PasswordResetConfirmDto request = new PasswordResetConfirmDto();
        request.setEmail("not-an-email");
        request.setOtpCode("");
        request.setNewPassword("short");
        request.setConfirmationPassword("short");

        mockMvc.perform(post(API_AUTH_PASSWORD + CONFIRM_RESET_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}