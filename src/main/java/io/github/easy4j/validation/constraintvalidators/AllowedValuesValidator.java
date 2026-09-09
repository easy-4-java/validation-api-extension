package io.github.easy4j.validation.constraintvalidators;

import io.github.easy4j.validation.constraints.AllowableValues;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Validator for the {@link io.github.easy4j.validation.constraints.AllowableValues} constraint.
 *
 * <p>Splits the comma-separated {@code allows} value, trims each entry, and checks
 * whether the input is contained in the resulting set.  Blank values are accepted only
 * when {@code nullable = true}.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
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
