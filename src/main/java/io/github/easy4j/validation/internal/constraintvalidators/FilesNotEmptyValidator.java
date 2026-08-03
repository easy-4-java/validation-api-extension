package io.github.easy4j.validation.internal.constraintvalidators;

import io.github.easy4j.validation.constraints.FileNotEmpty;
import io.github.easy4j.validation.file.UploadFile;
import io.github.easy4j.validation.file.UploadFileAdapters;
import io.github.easy4j.validation.provider.FileContentCheckStrategy;
import io.github.easy4j.validation.utils.FileSizeUtil;
import io.github.easy4j.validation.utils.MimetypeUtil;
import io.github.easy4j.validation.utils.TikaUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.mime.MimeType;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 多文件校验。
 *
 * @author wandl
 * @version 1.0
 * @since 2022.11.07
 */
@Slf4j
public class FilesNotEmptyValidator implements ConstraintValidator<FileNotEmpty, Object[]> {

    private Set<String> extensionSet = new HashSet<String>();
    private Set<String> mimeTypeSet = new HashSet<String>();
    private Long maxSize;
    private boolean required;
    private boolean strict;

    @Inject
    private FileContentCheckStrategy contentCheckStrategy;

    @Override
    public void initialize(FileNotEmpty annotation) {
        this.extensionSet = ArrayUtils.isNotEmpty(annotation.extensions())
                ? Stream.of(annotation.extensions()).map(String::toLowerCase).collect(Collectors.toSet())
                : Collections.emptySet();
        this.mimeTypeSet = ArrayUtils.isNotEmpty(annotation.mimeTypes())
                ? Stream.of(annotation.mimeTypes()).map(String::toLowerCase).collect(Collectors.toSet())
                : Collections.emptySet();
        this.required = annotation.required();
        this.strict = annotation.strict();
        this.maxSize = StringUtils.isNotBlank(annotation.maxSize())
                ? FileSizeUtil.parse(annotation.maxSize()) : null;
    }

    @Override
    public boolean isValid(Object[] values, ConstraintValidatorContext context) {
        // 1、验证文件是否为空
        if (Objects.isNull(values) || values.length == 0) {
            return !required;
        }
        // 2、验证文件后缀和 content type 是否满足要求
        if (extensionSet.isEmpty() && mimeTypeSet.isEmpty()) {
            return Boolean.TRUE;
        }
        // 3、循环验证文件
        for (Object value : values) {
            // 适配层只负责将具体框架文件转换为公共上传文件接口，后续保留生产校验逻辑。
            UploadFile uploadFile = UploadFileAdapters.adapt(value);
            if (Objects.isNull(uploadFile)) {
                return false;
            }
            // 3.1、验证文件大小是否满足要求
            if (Objects.nonNull(maxSize) && maxSize <= uploadFile.getSize()) {
                return Boolean.FALSE;
            }
            try {
                // 4、判断是否严格模式
                if (strict) {
                    // 4.1、首先尝试使用 Apache Tika 解析文件类型
                    MimeType detectMimeType = TikaUtil.detectMimeType(uploadFile.getInputStream());
                    if (Objects.nonNull(detectMimeType)
                            && StringUtils.isNotBlank(detectMimeType.getExtension())
                            && StringUtils.isNotBlank(detectMimeType.getName())) {
                        String extension = FilenameUtils.getExtension(detectMimeType.getExtension());
                        // 4.1.1、验证文件后缀是否满足要求
                        if (!extensionSet.isEmpty()
                                && !extensionSet.contains(extension.toLowerCase())) {
                            return Boolean.FALSE;
                        }
                        // 4.1.2、验证文件 content type 是否满足要求
                        if (!mimeTypeSet.isEmpty()
                                && !mimeTypeSet.contains(detectMimeType.getName().toLowerCase())) {
                            return Boolean.FALSE;
                        }
                        // 4.1.3、验证文件内容
                        if (Objects.nonNull(contentCheckStrategy)
                                && !contentCheckStrategy.check(extension, uploadFile)) {
                            return Boolean.FALSE;
                        }
                        // 5、验证通过，进行下一个文件的验证
                        continue;
                    }
                    // Apache Tika 解析为空，则会继续执行后续逻辑
                }
                // 4.2、使用 MimetypesFileTypeMap 解析文件类型
                String extension = FilenameUtils.getExtension(uploadFile.getOriginalFilename());
                // 4.2.1、验证文件后缀是否满足要求
                if (!extensionSet.isEmpty() && !extensionSet.contains(extension.toLowerCase())) {
                    return Boolean.FALSE;
                }
                // 4.2.2、验证文件 content type 是否满足要求
                if (!mimeTypeSet.isEmpty()) {
                    String mimeType = MimetypeUtil.detectMimeType(uploadFile.getOriginalFilename());
                    if (StringUtils.isNotBlank(mimeType)
                            && !mimeTypeSet.contains(mimeType.toLowerCase())) {
                        return Boolean.FALSE;
                    }
                }
                // 4.2.3、验证文件内容
                if (Objects.nonNull(contentCheckStrategy)
                        && !contentCheckStrategy.check(extension, uploadFile)) {
                    return Boolean.FALSE;
                }
            } catch (Exception exception) {
                log.error(exception.getMessage(), exception);
                return Boolean.FALSE;
            }
        }
        // 4、验证通过
        return Boolean.TRUE;
    }
}
