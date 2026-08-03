package io.github.easy4j.validation.constraints;

import io.github.easy4j.validation.constraintvalidators.FileNotEmptyValidator;
import io.github.easy4j.validation.constraintvalidators.FilesNotEmptyValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 校验上传文件是否存在，并按需校验大小、扩展名、MIME 类型和真实文件头。
 */
@Documented
@Constraint(validatedBy = {FileNotEmptyValidator.class, FilesNotEmptyValidator.class})
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE,
        ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface FileNotEmpty {

    String message() default "文件格式不正确";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * 允许的文件扩展名，不区分大小写且可以带点号。
     *
     * @return 扩展名列表
     */
    String[] extensions() default {};

    /**
     * 文件是否必填。
     *
     * @return 是否必填
     */
    boolean required() default true;

    /**
     * 单个文件最大大小，支持 B、KB、MB、GB、TB。
     *
     * @return 最大文件大小
     */
    String maxSize() default "2MB";

    /**
     * 允许的 MIME 类型，不区分大小写。
     *
     * @return MIME 类型列表
     */
    String[] mimeTypes() default {};

    /**
     * 是否使用 Tika 校验真实文件头及容器类型。
     *
     * @return 是否严格校验
     */
    boolean strict() default false;

}
