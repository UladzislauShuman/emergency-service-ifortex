package ifortex.shuman.uladzislau.authservice.annotation.validation;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static ifortex.shuman.uladzislau.authservice.TestConstanst.INVALID_PHONE;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.PHONE_WITHOUT_PLUS;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.UNPARSABLE_PHONE_NUMBER;
import static ifortex.shuman.uladzislau.authservice.TestConstanst.VALID_PHONE_NUMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PhoneNumberValidatorTest {

    @Mock
    private PhoneNumberUtil phoneNumberUtil;

    @InjectMocks
    private PhoneNumberValidator phoneNumberValidator;

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    void isValid_whenPhoneIsNullOrBlank_shouldReturnTrue(String phoneInput) {
        boolean result = phoneNumberValidator.isValid(phoneInput, null);

        assertThat(result).isTrue();

        verifyNoInteractions(phoneNumberUtil);
    }

    @Test
    void isValid_whenPhoneDoesNotStartWithPlus_shouldReturnFalse() {
        String phoneWithoutPlus = PHONE_WITHOUT_PLUS;

        boolean result = phoneNumberValidator.isValid(phoneWithoutPlus, null);

        assertThat(result).isFalse();
        verifyNoInteractions(phoneNumberUtil);
    }

    @Test
    void isValid_whenPhoneNumberParsingFails_shouldReturnFalse() throws NumberParseException {
        String unparsablePhone = UNPARSABLE_PHONE_NUMBER;
        when(phoneNumberUtil.parse(unparsablePhone, null))
                .thenThrow(new NumberParseException(NumberParseException.ErrorType.NOT_A_NUMBER, "Test exception"));

        boolean result = phoneNumberValidator.isValid(unparsablePhone, null);

        assertThat(result).isFalse();
        verify(phoneNumberUtil, never()).isValidNumber(any());
    }

    @Test
    void isValid_whenPhoneNumberIsParsedButInvalid_shouldReturnFalse() throws NumberParseException {
        String invalidPhone = INVALID_PHONE;
        PhoneNumber parsedNumber = new PhoneNumber();

        when(phoneNumberUtil.parse(eq(invalidPhone), isNull())).thenReturn(parsedNumber);
        when(phoneNumberUtil.isValidNumber(parsedNumber)).thenReturn(false);

        boolean result = phoneNumberValidator.isValid(invalidPhone, null);

        assertThat(result).isFalse();
        verify(phoneNumberUtil).parse(eq(invalidPhone), isNull());
        verify(phoneNumberUtil).isValidNumber(parsedNumber);
    }

    @Test
    void isValid_whenPhoneNumberIsValid_shouldReturnTrue() throws NumberParseException {
        String validPhone = VALID_PHONE_NUMBER;

        PhoneNumber parsedNumber = new PhoneNumber();

        when(phoneNumberUtil.parse(eq(validPhone), isNull())).thenReturn(parsedNumber);
        when(phoneNumberUtil.isValidNumber(parsedNumber)).thenReturn(true);

        boolean result = phoneNumberValidator.isValid(validPhone, null);

        assertThat(result).isTrue();
        verify(phoneNumberUtil).parse(eq(validPhone), isNull());
        verify(phoneNumberUtil).isValidNumber(parsedNumber);
    }
}