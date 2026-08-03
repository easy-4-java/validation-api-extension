package io.github.easy4j.validation.constraints;

import io.github.easy4j.validation.constraintvalidators.PhoneValueValidator;
import javax.validation.Constraint;
import javax.validation.Payload;

import java.lang.annotation.*;

/**
 * 手机号校验注解
 *
 * <p>校验字符串是否为有效的手机号码格式，支持国际区号。
 *
 * @author <a href="https://github.com/partme-ai">PartMe.AI</a>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Constraint(validatedBy = {PhoneValueValidator.class})
public @interface PhoneNumber {

    String lang() default "CN";

    String value() default "";

    String message();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
