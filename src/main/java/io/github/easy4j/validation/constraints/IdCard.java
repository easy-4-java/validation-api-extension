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

import io.github.easy4j.validation.constraintvalidators.IdCardValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Constraint annotation that validates whether a string is a valid Chinese national ID card
 * number (15-digit or 18-digit format, including mainland, Hong Kong, and Taiwan variants).
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see io.github.easy4j.validation.constraintvalidators.IdCardValidator
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE})
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = IdCardValidator.class)
public @interface IdCard {

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

}
