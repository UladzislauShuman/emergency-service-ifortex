package ifortex.shuman.uladzislau.authservice.annotation.validation;

import ifortex.shuman.uladzislau.authservice.annotation.validation.messages.ValidationMessages;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PhoneNumberValidator.class)
@Documented
public @interface PhoneNumber {
    String message() default ValidationMessages.PHONE_INVALID_FORMAT;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}