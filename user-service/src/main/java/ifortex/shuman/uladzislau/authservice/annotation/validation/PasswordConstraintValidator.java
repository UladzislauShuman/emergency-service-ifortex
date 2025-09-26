package ifortex.shuman.uladzislau.authservice.annotation.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Pattern;

public class PasswordConstraintValidator implements ConstraintValidator<Password, String> {

  public static final int MIN_LENGTH = 8;

  @Override
  public boolean isValid(String password, ConstraintValidatorContext context) {
    if (password == null || password.isBlank()) {
      return true;
    }

    List<String> errors = new ArrayList<>();

    // length
    if (password.length() < MIN_LENGTH) {
      errors.add("must be at least 8 characters long");
    }

    // uppercase letter
    if (!Pattern.compile("[A-Z]").matcher(password).find()) {
      errors.add("must contain at least one uppercase letter");
    }

    // 3. lowercase letter
    if (!Pattern.compile("[a-z]").matcher(password).find()) {
      errors.add("must contain at least one lowercase letter");
    }

    // 4. digit
    if (!Pattern.compile("[0-9]").matcher(password).find()) {
      errors.add("must contain at least one digit");
    }

    // 5. digit
    if (!Pattern.compile("[@#$%^&+=!]").matcher(password).find()) {
      errors.add("must contain at least one digit(@#$%^&+=!)");
    }

    if (errors.isEmpty()) {
      return true;
    }

    StringJoiner joiner = new StringJoiner(", ", "Password ", ".");
    errors.forEach(joiner::add);
    String message = joiner.toString();

    context.disableDefaultConstraintViolation();
    context.buildConstraintViolationWithTemplate(message).addConstraintViolation();

    return false;
  }
}