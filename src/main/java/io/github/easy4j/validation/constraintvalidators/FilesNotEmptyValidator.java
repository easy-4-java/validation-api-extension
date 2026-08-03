package io.github.easy4j.validation.constraintvalidators;

import io.github.easy4j.validation.constraints.FileNotEmpty;
import io.github.easy4j.validation.provider.FileContentCheckStrategy;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;

/**
 * 多文件校验器。
 */
public final class FilesNotEmptyValidator implements ConstraintValidator<FileNotEmpty, Object[]> {

    @Inject
    private FileContentCheckStrategy contentCheckStrategy;
    private FileValidationSupport support;

    @Override
    public void initialize(FileNotEmpty annotation) {
        this.support = new FileValidationSupport(annotation);
    }

    @Override
    public boolean isValid(Object[] values, ConstraintValidatorContext context) {
        if (Objects.isNull(values) || values.length == 0) {
            return !support.isRequired();
        }
        for (Object value : values) {
            if (!support.isValid(value, contentCheckStrategy)) {
                return false;
            }
        }
        return true;
    }
}
