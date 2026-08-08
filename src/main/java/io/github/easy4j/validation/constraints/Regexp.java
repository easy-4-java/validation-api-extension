/*
 * Copyright (c) 2018, Loong Wan (https://github.com/loong10k).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.github.easy4j.validation.constraints;

import io.github.easy4j.validation.constraintvalidators.RegexpValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.apache.oro.text.regex.Perl5Compiler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Constraint annotation that validates whether a string field <em>fully matches</em> a Perl 5
 * regular expression (Apache ORO engine).
 *
 * <p>This differs from {@link Contains} in that the entire string must match, not just a
 * substring.  The matching engine is Apache ORO Perl5.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see io.github.easy4j.validation.constraintvalidators.RegexpValidator
 * @see Contains
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RegexpValidator.class)
public @interface Regexp {

	/**
	 * The Perl 5 regular expression that the value must fully match.
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
