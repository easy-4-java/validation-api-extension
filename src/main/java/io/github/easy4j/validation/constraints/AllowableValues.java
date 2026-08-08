package io.github.easy4j.validation.constraints;


import io.github.easy4j.validation.constraintvalidators.AllowedValuesValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Constraint annotation that validates a field value against a configurable set of allowed values.
 *
 * <p>The allowed values are supplied via {@link #allows()} as a comma-separated string and are
 * trimmed before comparison.  When {@link #nullable()} is {@code true}, blank or {@code null}
 * values are considered valid; otherwise the value must appear in the allowed set.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see io.github.easy4j.validation.constraintvalidators.AllowedValuesValidator
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Constraint(validatedBy = AllowedValuesValidator.class)
public @interface AllowableValues {

    /**
     * @return the error message template shown when validation fails.
     */
    String message() default "invalid values";

    /**
     * Comma-separated list of allowed values.  Leading and trailing whitespace around each
     * entry is trimmed before comparison.
     *
     * @return the comma-separated allowed values string
     */
    String allows() default "";

    /**
     * When {@code true}, blank or {@code null} input values are considered valid.
     *
     * @return whether {@code null} / blank values are accepted
     */
    boolean nullable() default false;

    /**
     * @return the validation groups this constraint belongs to
     */
    Class<?>[] groups() default {};

    /**
     * @return the payload associated with this constraint
     */
    Class<? extends Payload>[] payload() default {};

}
