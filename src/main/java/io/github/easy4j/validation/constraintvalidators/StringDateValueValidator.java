package io.github.easy4j.validation.constraintvalidators;

import io.github.easy4j.validation.constraints.StringDateValue;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;

/**
 * Validator for the {@link io.github.easy4j.validation.constraints.StringDateValue} constraint.
 *
 * <p>Parses the input string using {@link java.text.SimpleDateFormat} in non-lenient mode
 * and verifies that the entire string is consumed.  Blank values are considered valid.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
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
