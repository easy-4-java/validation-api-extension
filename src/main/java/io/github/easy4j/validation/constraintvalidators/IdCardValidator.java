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

import io.github.easy4j.validation.constraints.IdCard;
import io.github.easy4j.validation.utils.IDCardUtils;
import io.github.easy4j.validation.utils.IdcardUtils2;
import org.apache.commons.lang3.StringUtils;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.function.BooleanSupplier;

public class IdCardValidator implements ConstraintValidator<IdCard, String> {

	@Override
	public void initialize(IdCard annotation) {
	}

	@Override
	public boolean isValid(String input, ConstraintValidatorContext context) {
		if (StringUtils.isBlank(input)) {
			return true;
		}
		return safely(() -> IDCardUtils.isIDCard(input))
				|| safely(() -> IdcardUtils2.validateCard(input))
				|| safely(() -> IdcardUtils2.validateHKCard(input))
				|| safely(() -> IdcardUtils2.validateTWCard(input));
	}

	private boolean safely(BooleanSupplier validator) {
		try {
			return validator.getAsBoolean();
		} catch (RuntimeException ignored) {
			return false;
		}
	}

}
