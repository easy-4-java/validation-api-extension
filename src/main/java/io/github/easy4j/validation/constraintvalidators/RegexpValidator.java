/*
 * Copyright (c) 2018, hiwepy (https://github.com/hiwepy).
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

import io.github.easy4j.validation.constraints.Regexp;
import io.github.easy4j.validation.utils.JakartaOROUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;


public class RegexpValidator implements ConstraintValidator<Regexp, String>{

	private String pattern;
	/**
     * CASE_INSENSITIVE_MASK : 区分大小写
     * DEFAULT_MASK : 默认(不区分大小写)
     * EXTENDED_MASK : 支持Perl5 扩展正则表达式
     * MULTILINE_MASK : 多行匹配，^$匹配每行内容．
     * SINGLELINE_MASK　：单行匹配  ^$匹配全部内容.
     * READ_ONLY_MASK : Perl5Pattern 是只读的，提高性能且线程安全．
     */
	private int mask;

	@Override
	public void initialize(Regexp annotation) {
		this.pattern = annotation.pattern();
		this.mask = annotation.mask();
	}

	@Override
	public boolean isValid(String input, ConstraintValidatorContext context) {
		if(StringUtils.isNotBlank(input)){
			// 返回匹配结果
			return JakartaOROUtils.matches(pattern, mask, input);
		}
		return true;
	}

}
