package ifortex.shuman.uladzislau.authservice.controller.frontend;

import ifortex.shuman.uladzislau.authservice.dto.AdminPasswordResetConfirmDto;
import ifortex.shuman.uladzislau.authservice.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static ifortex.shuman.uladzislau.authservice.TestConstanst.TEST_UUID_RESET_TOKEN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class PasswordResetPageControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private AuthenticationService authenticationService;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void showResetPasswordForm_shouldReturnOkAndRenderForm_whenTokenIsPresent() throws Exception {
        mockMvc.perform(get("/password/reset/form")
                        .param("token", TEST_UUID_RESET_TOKEN)
                        .with(anonymous()))
                .andExpect(status().isOk())
                .andExpect(view().name("reset-password-form"))
                .andExpect(model().attribute("token", TEST_UUID_RESET_TOKEN));
    }

    @Test
    void processResetPassword_shouldSucceedAndRedirect_whenPasswordsMatchAndTokenIsValid() throws Exception {
        doNothing().when(authenticationService).confirmAdminPasswordReset(any(AdminPasswordResetConfirmDto.class));

        mockMvc.perform(post("/password/reset/confirm")
                        .param("token", TEST_UUID_RESET_TOKEN)
                        .param("newPassword", "NewSecurePassword1!")
                        .param("confirmationPassword", "NewSecurePassword1!")
                        .with(csrf())
                        .with(anonymous()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("success", "Your password has been reset successfully! You can now log in."))
                .andExpect(redirectedUrl("/password/reset/form?token=" + TEST_UUID_RESET_TOKEN));

        verify(authenticationService).confirmAdminPasswordReset(any(AdminPasswordResetConfirmDto.class));
    }

    @Test
    void processResetPassword_shouldFailAndRedirect_whenPasswordsDoNotMatch() throws Exception {
        mockMvc.perform(post("/password/reset/confirm")
                        .param("token", TEST_UUID_RESET_TOKEN)
                        .param("newPassword", "passwordA")
                        .param("confirmationPassword", "passwordB")
                        .with(csrf())
                        .with(anonymous()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("error", "Error: Passwords do not match."))
                .andExpect(redirectedUrl("/password/reset/form?token=" + TEST_UUID_RESET_TOKEN));

        verify(authenticationService, never()).confirmAdminPasswordReset(any());
    }

    @Test
    void processResetPassword_shouldFailAndRedirect_whenServiceThrowsException() throws Exception {
        String serviceErrorMessage = "Invalid or expired token.";
        doThrow(new IllegalArgumentException(serviceErrorMessage))
                .when(authenticationService).confirmAdminPasswordReset(any(AdminPasswordResetConfirmDto.class));

        mockMvc.perform(post("/password/reset/confirm")
                        .param("token", TEST_UUID_RESET_TOKEN)
                        .param("newPassword", "NewSecurePassword1!")
                        .param("confirmationPassword", "NewSecurePassword1!")
                        .with(csrf())
                        .with(anonymous()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("error", "Error: " + serviceErrorMessage))
                .andExpect(redirectedUrl("/password/reset/form?token=" + TEST_UUID_RESET_TOKEN));

        verify(authenticationService).confirmAdminPasswordReset(any(AdminPasswordResetConfirmDto.class));
    }
}