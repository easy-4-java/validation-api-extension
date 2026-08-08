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
package io.github.easy4j.validation.constraintvalidators;

import io.github.easy4j.validation.constraints.Contains;
import io.github.easy4j.validation.utils.JakartaOROUtils;
import org.apache.commons.lang3.StringUtils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


/**
 * Validator for the {@link io.github.easy4j.validation.constraints.Contains} constraint.
 *
 * <p>Uses the Apache ORO Perl5 engine to check whether the input string contains a match
 * for the configured regular expression.  Blank or empty inputs are considered valid.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 */
public class ContainsValidator implements ConstraintValidator<Contains, String>{

	private String pattern;
	/** Compiled pattern mask flags. */
	private int mask;

	@Override
	public void initialize(Contains annotation) {
		this.pattern = annotation.pattern();
		this.mask = annotation.mask();
	}

	@Override
	public boolean isValid(String input, ConstraintValidatorContext context) {
		if(StringUtils.isNotBlank(input)){
			// 返回匹配结果
			return JakartaOROUtils.contains(pattern, mask, input);
		}
		return true;
	}

}
