package io.github.easy4j.validation.constraintvalidators;

import io.github.easy4j.validation.constraints.FileNotEmpty;
import io.github.easy4j.validation.provider.FileContentCheckStrategy;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

/**
 * 多文件校验。
 *
 * @author wandl
 * @version 1.0
 * @since 2022.11.07
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
