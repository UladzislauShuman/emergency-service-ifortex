package ifortex.shuman.uladzislau.authservice.annotation.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static ifortex.shuman.uladzislau.authservice.annotation.validation.messages.ValidationMessages.*;


@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {PasswordConstraintValidator.class})
public @interface Password {

    String message() default "Invalid password format";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}