package io.github.easy4j.validation.constraintvalidators;

import io.github.easy4j.validation.constraints.StringDateValue;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;

/**
 * 字符串日期格式校验器
 *
 * @author <a href="https://github.com/partme-ai">PartMe.AI</a>
 * @since 2021-03-08
 */
public class StringDateValueValidator implements ConstraintValidator<StringDateValue, String> {

    private StringDateValue dateValue;

    @Override
    public void initialize(StringDateValue annotation) {
        this.dateValue = annotation;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if (StringUtils.isBlank(value)) {
            return true;
        }
        boolean valid = false;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(dateValue.pattern());
            dateFormat.setLenient(false);
            ParsePosition position = new ParsePosition(0);
            valid = dateFormat.parse(value, position) != null && position.getIndex() == value.length();
        } catch (IllegalArgumentException exception) {
            valid = false;
        }
        if (!valid) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(dateValue.message()).addConstraintViolation();
        }
        return valid;
    }
}
