package io.github.easy4j.validation.constraintvalidators;


import io.github.easy4j.validation.constraints.NumberValue;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Objects;

/**
 * Validator for the {@link io.github.easy4j.validation.constraints.NumberValue} constraint.
 *
 * <p>Compiles the configured regex and checks whether the entire input string matches.
 * {@code null} values are considered valid.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 */
public class NumberValueValidator implements ConstraintValidator<NumberValue, String> {

    private NumberValue numberValue;
    private Pattern pattern;

    @Override
    public void initialize(NumberValue annotation) {
        this.numberValue = annotation;
        this.pattern = Pattern.compile(annotation.regex());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if (Objects.isNull(value)) {
            return true;
        }
        boolean flag = validate(value);
        if (!flag) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(numberValue.message()).addConstraintViolation();
        }
        return flag;
    }

    private boolean validate(String str) {
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }
}
