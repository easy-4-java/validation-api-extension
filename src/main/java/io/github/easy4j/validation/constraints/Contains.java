package io.github.easy4j.validation.constraints;

import io.github.easy4j.validation.constraintvalidators.ContainsValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.apache.oro.text.regex.Perl5Compiler;

import java.lang.annotation.*;
/**
 * Constraint annotation that validates whether a string field or parameter <em>contains</em>
 * a match for the given Perl 5 regular expression.
 *
 * <p>This differs from {@link Regexp} in that the value only needs to contain a match
 * (via {@code contains}) rather than matching the entire string.  The matching engine is
 * Apache ORO Perl5.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see io.github.easy4j.validation.constraintvalidators.ContainsValidator
 * @see Regexp
 */
@Target({ ElementType.FIELD,ElementType.PARAMETER})
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ContainsValidator.class)
public @interface Contains {

	/**
	 * The Perl 5 regular expression that the value must contain.
	 *
	 * @return the regular expression to match
	 */
	String pattern();
	/**
	 * @return the error message template
	 */
	String message() default "{jakarta.validation.constraints.Pattern.message}";

	/**
	 * @return the validation groups this constraint belongs to
	 */
	Class<?>[] groups() default {};

	/**
	 * @return the payload associated with this constraint
	 */
	Class<? extends Payload>[] payload() default {};

	/**
	 * Compilation mask for the Perl 5 pattern.
	 * <ul>
	 *   <li>{@code CASE_INSENSITIVE_MASK} &ndash; case-sensitive matching</li>
	 *   <li>{@code DEFAULT_MASK} &ndash; default (case-insensitive)</li>
	 *   <li>{@code EXTENDED_MASK} &ndash; support Perl 5 extended expressions</li>
	 *   <li>{@code MULTILINE_MASK} &ndash; multiline; {@code ^$} match each line</li>
	 *   <li>{@code SINGLELINE_MASK} &ndash; single-line; {@code ^$} match entire content</li>
	 *   <li>{@code READ_ONLY_MASK} &ndash; make compiled pattern read-only (thread-safe)</li>
	 * </ul>
	 *
	 * @return the OR mask flags for the Perl 5 compiler
	 */
	int mask() default Perl5Compiler.CASE_INSENSITIVE_MASK;

}
