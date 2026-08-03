package io.github.easy4j.validation.constraintvalidators;


import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import io.github.easy4j.validation.constraints.PhoneNumber;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import java.util.Objects;

/**
 * 数据校验注解实现类
 *
 * @author <a href="https://github.com/partme-ai">PartMe.AI</a>
 * @since 2021-03-08
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
