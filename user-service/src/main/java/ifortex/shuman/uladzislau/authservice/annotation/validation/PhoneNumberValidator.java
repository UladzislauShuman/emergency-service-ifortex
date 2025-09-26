package ifortex.shuman.uladzislau.authservice.annotation.validation;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PhoneNumberValidator
        implements ConstraintValidator
        <ifortex.shuman.uladzislau.authservice.annotation.validation.PhoneNumber, String> {

    private final PhoneNumberUtil phoneNumberUtil;

    @Override
    public boolean isValid(String phone, ConstraintValidatorContext context) {
        if (phone == null || phone.isBlank()) {
            return true;
        }
        if (!phone.startsWith("+")) {
            return false;
        }
        try {
            PhoneNumber phoneNumber = phoneNumberUtil.parse(phone, null);
            return phoneNumberUtil.isValidNumber(phoneNumber);
        } catch (NumberParseException e) {
            return false;
        }
    }
}