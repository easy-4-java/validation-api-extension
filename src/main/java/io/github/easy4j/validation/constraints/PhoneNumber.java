package io.github.easy4j.validation.constraints;

import io.github.easy4j.validation.constraintvalidators.PhoneValueValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Constraint annotation that validates whether a string is a valid phone number.
 *
 * <p>Uses Google's {@code libphonenumber} library for parsing and validation.
 * The default region is {@code "CN"} (China) and can be overridden via {@link #lang()}.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see io.github.easy4j.validation.constraintvalidators.PhoneValueValidator
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Constraint(validatedBy = {PhoneValueValidator.class})
public @interface PhoneNumber {

    /**
     * The default region code (ISO 3166-1 alpha-2) used when the phone number does not
     * include an international dialling prefix.
     *
     * @return the region code (e.g. {@code "CN"}, {@code "US"})
     */
    String lang() default "CN";

    /**
     * Reserved for future use.
     *
     * @return an optional value attribute
     */
    String value() default "";

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
