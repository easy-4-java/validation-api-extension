package io.github.easy4j.validation.constraintvalidators;


import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import io.github.easy4j.validation.constraints.PhoneNumber;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Objects;

/**
 * Validator for the {@link io.github.easy4j.validation.constraints.PhoneNumber} constraint.
 *
 * <p>Uses Google's {@code libphonenumber} to parse and validate the phone number against
 * the configured default region.  {@code null} values are considered valid.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 */
public class PhoneValueValidator implements ConstraintValidator<PhoneNumber, String> {

    private static final PhoneNumberUtil PHONE_NUMBER_UTIL = PhoneNumberUtil.getInstance();

    private PhoneNumber phoneValue;

    @Override
    public void initialize(PhoneNumber annotation) {
        this.phoneValue = annotation;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if (Objects.isNull(value)) {
            return true;
        }
        try {
            Phonenumber.PhoneNumber referencePhoneNumber = PHONE_NUMBER_UTIL.parse(value, phoneValue.lang());
            boolean flag = PHONE_NUMBER_UTIL.isValidNumber(referencePhoneNumber);
            if (!flag) {
                constraintValidatorContext.disableDefaultConstraintViolation();
                constraintValidatorContext.buildConstraintViolationWithTemplate(phoneValue.message())
                        .addConstraintViolation();
            }
            return flag;
        } catch (NumberParseException e) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(phoneValue.message())
                    .addConstraintViolation();
            return false;
        }
    }

}
