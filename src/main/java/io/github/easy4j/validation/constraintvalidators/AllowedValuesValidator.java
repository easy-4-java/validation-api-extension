package io.github.easy4j.validation.constraintvalidators;

import io.github.easy4j.validation.constraints.AllowableValues;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 验证值是否在指定范围内
 *
 * @author <a href="https://github.com/partme-ai">PartMe.AI</a>
 * @since 2021-03-08
 */
public class AllowedValuesValidator implements ConstraintValidator<AllowableValues, String> {

    private Set<String> allows;
    private boolean nullable;

    @Override
    public void initialize(AllowableValues annotation) {
        nullable = annotation.nullable();
        String[] values = StringUtils.split(annotation.allows(), ',');
        if (values == null || values.length == 0) {
            allows = Collections.emptySet();
            return;
        }
        Set<String> normalizedValues = new LinkedHashSet<String>();
        for (String value : values) {
            if (StringUtils.isNotBlank(value)) {
                normalizedValues.add(StringUtils.trim(value));
            }
        }
        allows = Collections.unmodifiableSet(normalizedValues);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (nullable && StringUtils.isBlank(value)) {
            return true;
        }
        return allows.contains(value);
    }
}
