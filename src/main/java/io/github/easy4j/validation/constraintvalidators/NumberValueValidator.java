package io.github.easy4j.validation.constraintvalidators;


import io.github.easy4j.validation.constraints.NumberValue;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Objects;

/**
 * 数据校验注解实现类
 *
 * @author <a href="https://github.com/partme-ai">PartMe.AI</a>
 * @since 2021-03-08
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
