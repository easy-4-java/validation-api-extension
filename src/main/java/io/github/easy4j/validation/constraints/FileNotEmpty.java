package io.github.easy4j.validation.constraints;

import io.github.easy4j.validation.constraintvalidators.FileNotEmptyValidator;
import io.github.easy4j.validation.constraintvalidators.FilesNotEmptyValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Constraint annotation that validates the presence and basic integrity of an uploaded file.
 *
 * <p>Supports both single-file ({@link io.github.easy4j.validation.file.UploadFile}) and
 * multi-file ({code UploadFile[]}) fields.  The annotation optionally enforces maximum file
 * size, allowed file extensions, MIME types, and (in strict mode) real file-header detection
 * via Apache Tika.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see io.github.easy4j.validation.constraintvalidators.FileNotEmptyValidator
 * @see io.github.easy4j.validation.constraintvalidators.FilesNotEmptyValidator
 */
@Documented
@Constraint(validatedBy = {FileNotEmptyValidator.class, FilesNotEmptyValidator.class})
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE,
        ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface FileNotEmpty {

    /**
     * @return the error message template shown when validation fails
     */
    String message() default "文件格式不正确";

    /**
     * @return the validation groups this constraint belongs to
     */
    Class<?>[] groups() default {};

    /**
     * @return the payload associated with this constraint
     */
    Class<? extends Payload>[] payload() default {};

    /**
     * Allowed file extensions (case-insensitive, may include a leading dot).
     *
     * @return the list of allowed extensions
     */
    String[] extensions() default {};

    /**
     * Whether the file is required (must be non-empty).
     *
     * @return {@code true} if the file must be present and non-empty
     */
    boolean required() default true;

    /**
     * Maximum size per file, supporting units B, KB, MB, GB, TB.
     *
     * @return the maximum file size string (e.g. {@code "2MB"})
     */
    String maxSize() default "2MB";

    /**
     * Allowed MIME types (case-insensitive).
     *
     * @return the list of allowed MIME types
     */
    String[] mimeTypes() default {};

    /**
     * When {@code true}, Apache Tika is used to detect the real file header and container
     * type instead of trusting the client-declared filename extension.
     *
     * @return whether strict file-header detection is enabled
     */
    boolean strict() default false;

}
