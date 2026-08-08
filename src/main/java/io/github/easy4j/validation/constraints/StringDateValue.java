package io.github.easy4j.validation.constraints;

import io.github.easy4j.validation.constraintvalidators.StringDateValueValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Constraint annotation that validates whether a string conforms to a date format pattern.
 *
 * <p>Uses {@link java.text.SimpleDateFormat} with <em>lenient</em> mode disabled so that
 * only strictly valid dates are accepted (e.g. {@code "2026-02-29"} is rejected in a
 * non-leap year).  The default pattern is {@code "yyyy-MM-dd"}.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see io.github.easy4j.validation.constraintvalidators.StringDateValueValidator
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Constraint(validatedBy = {StringDateValueValidator.class})
public @interface StringDateValue {

    /**
     * The date format pattern used by {@link java.text.SimpleDateFormat}.
     *
     * @return the date pattern (e.g. {@code "yyyy-MM-dd"})
     */
    String pattern() default "yyyy-MM-dd";

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
