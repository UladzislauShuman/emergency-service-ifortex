package ifortex.shuman.uladzislau.authservice.annotation.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Pointcut("within(ifortex.shuman.uladzislau.authservice.service..*)")
    public void serviceMethods() {}


    @Pointcut("@annotation(ifortex.shuman.uladzislau.authservice.annotation.logging.UserLogging)")
    public void loggableMethods() {}

    @Before("loggableMethods()")
    public void logMethodEntry(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().toShortString();
        String args = Arrays.stream(joinPoint.getArgs())
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
        
        String user = getCurrentUser();

        log.info("User [{}] is executing {} with arguments: [{}]", user, methodName, args);
    }

    private String getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            return "ANONYMOUS";
        }
        return authentication.getName();
    }
}