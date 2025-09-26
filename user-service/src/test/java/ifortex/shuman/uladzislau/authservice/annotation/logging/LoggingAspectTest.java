package ifortex.shuman.uladzislau.authservice.annotation.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
class LoggingAspectTest {

    public static class TestService {
        @UserLogging
        public void annotatedMethod(String s, int i) {}

        public void nonAnnotatedMethod() {}
    }

    private TestService proxy;

    @BeforeEach
    void setUp() {
        LoggingAspect aspect = new LoggingAspect();
        TestService targetService = new TestService();

        AspectJProxyFactory factory = new AspectJProxyFactory(targetService);
        factory.addAspect(aspect);

        proxy = factory.getProxy();

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should log method entry with username when called by an authenticated user")
    void logMethodEntry_whenCalledByAuthenticatedUser_shouldLogUsername(CapturedOutput output) {
        User testUser = new User("test-user@example.com", "password", Collections.emptyList());
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(testUser,
                null, testUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        proxy.annotatedMethod("arg1", 123);

        String logOutput = output.getOut();

        assertThat(logOutput)
                .contains("User [test-user@example.com]")
                .contains("is executing")
                .contains("annotatedMethod")
                .contains("with arguments: [arg1, 123]");
    }

    @Test
    @DisplayName("Should log method entry as ANONYMOUS when called without authentication")
    void logMethodEntry_whenCalledByAnonymousUser_shouldLogAnonymous(CapturedOutput output) {
        proxy.annotatedMethod("someArg", 456);

        String logOutput = output.getOut();

        assertThat(logOutput)
                .contains("User [ANONYMOUS]")
                .contains("is executing")
                .contains("annotatedMethod")
                .contains("with arguments: [someArg, 456]");
    }

    @Test
    @DisplayName("Should not log anything when a non-annotated method is called")
    void logMethodEntry_whenNonAnnotatedMethodIsCalled_shouldNotLog(CapturedOutput output) {
        proxy.nonAnnotatedMethod();

        String logOutput = output.getOut();
        assertThat(logOutput).doesNotContain("is executing");
    }
}