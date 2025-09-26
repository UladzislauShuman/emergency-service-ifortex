package ifortex.shuman.uladzislau.authservice.annotation.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.springframework.web.multipart.MultipartFile;

public class FileValidator implements ConstraintValidator<ValidFile, MultipartFile> {

    private Set<String> allowedTypes;

    @Override
    public void initialize(ValidFile constraintAnnotation) {
        this.allowedTypes = new HashSet<>(Arrays.asList(constraintAnnotation.allowedTypes()));
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null || file.isEmpty()) {
            return true;
        }

        String contentType = file.getContentType();
        if (contentType == null || !isAllowedType(contentType)) {
            context.disableDefaultConstraintViolation();

            String allowedTypesString = String.join(", ", allowedTypes);
            String message = String.format("Invalid file type '%s'. Allowed types are: %s.",
                contentType, allowedTypesString);

            context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
            return false;
        }

        return true;
    }

    private boolean isAllowedType(String contentType) {
        if (allowedTypes.isEmpty()) {
            return true;
        }
        return allowedTypes.contains(contentType);
    }
}