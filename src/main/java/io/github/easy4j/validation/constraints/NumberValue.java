package io.github.easy4j.validation.constraints;

import io.github.easy4j.validation.constraintvalidators.NumberValueValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Constraint annotation that validates whether a string matches a numeric regular expression
 * pattern.
 *
 * <p>The default pattern {@code "^[0-9\-]+$"} accepts digits and an optional leading minus
 * sign.  Override via {@link #regex()} for custom numeric formats.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see io.github.easy4j.validation.constraintvalidators.NumberValueValidator
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Constraint(validatedBy = {NumberValueValidator.class})
public @interface NumberValue {

    /**
     * The regular expression the value must fully match (anchored via {@code Matcher.matches()}).
     *
     * @return the numeric regex pattern
     */
    String regex() default "^[0-9\\-]+$";

    /**
     * @return the error message template shown when validation fails
     */
    String message();

    /**
     * @return the validation groups this constraint belongs to
     */
    Class<?>[] groups() default {};

    /**
     * @return the payload associated with this constraint
     */
    Class<? extends Payload>[] payload() default {};
}
