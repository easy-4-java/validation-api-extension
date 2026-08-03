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
 * @author hiwepy
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RegexpValidator.class)
public @interface Regexp {

	/**
	 * @return the regular expression to match
	 */
	String pattern();
	/**
	 * @return the error message template
	 */
	String message() default "{jakarta.validation.constraints.Pattern.message}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	/**
     * CASE_INSENSITIVE_MASK : 区分大小写
     * DEFAULT_MASK : 默认(不区分大小写)
     * EXTENDED_MASK : 支持Perl5 扩展正则表达式
     * MULTILINE_MASK : 多行匹配，^$匹配每行内容．
     * SINGLELINE_MASK　：单行匹配  ^$匹配全部内容.
     * READ_ONLY_MASK : Perl5Pattern 是只读的，提高性能且线程安全．
     */
	int mask() default Perl5Compiler.CASE_INSENSITIVE_MASK;

}
