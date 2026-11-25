package io.github.easy4j.validation.constraintvalidators;

import cn.hutool.core.io.unit.DataSize;
import cn.hutool.core.io.unit.DataUnit;
import io.github.easy4j.validation.constraints.FileNotEmpty;
import io.github.easy4j.validation.file.FileValidationPolicy;
import io.github.easy4j.validation.file.FileValidationService;
import io.github.easy4j.validation.file.TikaFileTypeDetector;
import io.github.easy4j.validation.file.UploadFile;
import io.github.easy4j.validation.file.UploadFileAdapters;
import io.github.easy4j.validation.provider.FileContentCheckStrategy;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/** 单文件和数组注解的适配器；所有文件规则由公共服务实现。 */
final class FileValidationEngine {
    private final FileValidationPolicy policy;
    private final FileValidationService service;

    FileValidationEngine(FileNotEmpty annotation, FileContentCheckStrategy contentChecks) {
        long maxSize = StringUtils.isNotBlank(annotation.maxSize())
                ? Math.max(0, DataSize.parse(annotation.maxSize(), DataUnit.BYTES).toBytes()) : Long.MAX_VALUE;
        this.policy = FileValidationPolicy.builder().constraintCompatibility(true)
                .required(annotation.required()).strict(annotation.strict()).maxSizeBytes(maxSize)
                .allowedExtensions(annotation.extensions()).allowedMimeTypes(annotation.mimeTypes()).build();
        this.service = new FileValidationService(new TikaFileTypeDetector(), contentChecks, (file, type) -> true);
    }

    boolean isValid(Object value) {
        if (Objects.isNull(value)) {
            return service.validate(null, policy).isValid();
        }
        UploadFile file = UploadFileAdapters.adapt(value);
        return Objects.nonNull(file) && service.validate(file, policy).isValid();
    }

    boolean isValid(Object[] values) {
        if (Objects.isNull(values) || values.length == 0) {
            return !policy.isRequired();
        }
        for (Object value : values) {
            if (!isValid(value)) {
                return false;
            }
        }
        return true;
    }
}
