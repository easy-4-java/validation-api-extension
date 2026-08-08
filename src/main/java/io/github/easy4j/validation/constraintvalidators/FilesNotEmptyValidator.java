package io.github.easy4j.validation.constraintvalidators;

import io.github.easy4j.validation.constraints.FileNotEmpty;
import io.github.easy4j.validation.provider.FileContentCheckStrategy;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;

/**
 * Validator for the {@link io.github.easy4j.validation.constraints.FileNotEmpty} constraint
 * when applied to an {@code Object[]} (multi-file) field.
 *
 * <p>Delegates all file safety checks to {@link FileValidationEngine}.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 */
public class FilesNotEmptyValidator implements ConstraintValidator<FileNotEmpty, Object[]> {

    @Inject
    private FileContentCheckStrategy contentCheckStrategy = FileContentCheckStrategy.load();

    private FileValidationEngine validationEngine;

    @Override
    public void initialize(FileNotEmpty annotation) {
        this.validationEngine = new FileValidationEngine(annotation, contentCheckStrategy);
    }

    @Override
    public boolean isValid(Object[] values, ConstraintValidatorContext context) {
        return Objects.nonNull(validationEngine) && validationEngine.isValid(values);
    }
}
