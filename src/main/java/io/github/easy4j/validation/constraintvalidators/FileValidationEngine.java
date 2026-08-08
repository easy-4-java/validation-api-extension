package io.github.easy4j.validation.constraintvalidators;

import cn.hutool.core.io.unit.DataSize;
import cn.hutool.core.io.unit.DataUnit;
import io.github.easy4j.validation.constraints.FileNotEmpty;
import io.github.easy4j.validation.file.UploadFile;
import io.github.easy4j.validation.file.UploadFileAdapters;
import io.github.easy4j.validation.provider.FileContentCheckStrategy;
import io.github.easy4j.validation.utils.MimetypeUtil;
import io.github.easy4j.validation.utils.TikaUtil;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.mime.MimeType;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Unified validation engine for the {@link io.github.easy4j.validation.constraints.FileNotEmpty}
 * constraint.
 *
 * <p>Both the single-file and multi-file validators delegate all file safety checks (size,
 * extension, MIME type, and optional strict Tika-based content detection) to this class so
 * that the validation logic is defined in exactly one place.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 */
final class FileValidationEngine {

    private static final String GENERIC_MICROSOFT_OFFICE_MIME_TYPE = "application/x-tika-msoffice";
    private static final String MICROSOFT_WORD_MIME_TYPE = "application/msword";

    private final Set<String> extensionSet;
    private final Set<String> mimeTypeSet;
    private final DataSize maxSize;
    private final boolean required;
    private final boolean strict;
    private final FileContentCheckStrategy contentCheckStrategy;

    FileValidationEngine(FileNotEmpty annotation, FileContentCheckStrategy contentCheckStrategy) {
        this.extensionSet = normalizeSet(annotation.extensions());
        this.mimeTypeSet = normalizeSet(annotation.mimeTypes());
        this.maxSize = StringUtils.isNotBlank(annotation.maxSize())
                ? DataSize.parse(annotation.maxSize(), DataUnit.BYTES) : null;
        this.required = annotation.required();
        this.strict = annotation.strict();
        this.contentCheckStrategy = Objects.requireNonNull(contentCheckStrategy,
                "contentCheckStrategy must not be null");
    }

    boolean isValid(Object value) {
        if (Objects.isNull(value)) {
            return !required;
        }
        UploadFile uploadFile = UploadFileAdapters.adapt(value);
        return Objects.nonNull(uploadFile) && isValid(uploadFile);
    }

    boolean isValid(Object[] values) {
        if (Objects.isNull(values) || values.length == 0) {
            return !required;
        }
        for (Object value : values) {
            if (!isValid(value)) {
                return false;
            }
        }
        return true;
    }

    private boolean isValid(UploadFile uploadFile) {
        if (uploadFile.isEmpty()) {
            return !required;
        }
        if (Objects.nonNull(maxSize) && maxSize.compareTo(DataSize.of(uploadFile.getSize(), DataUnit.BYTES)) < 0) {
            return false;
        }
        if (extensionSet.isEmpty() && mimeTypeSet.isEmpty()) {
            return checkContent("", uploadFile);
        }

        String originalExtension = normalize(FilenameUtils.getExtension(uploadFile.getOriginalFilename()));
        if (!extensionSet.isEmpty() && !extensionSet.contains(originalExtension)) {
            return false;
        }
        try {
            return strict ? validateStrict(uploadFile, originalExtension) : validateByFilename(uploadFile, originalExtension);
        } catch (IOException exception) {
            return false;
        }
    }

    private boolean validateStrict(UploadFile uploadFile, String originalExtension) throws IOException {
        MimeType detectedMimeType = TikaUtil.detectContentMimeType(uploadFile);
        if (Objects.isNull(detectedMimeType) || StringUtils.isBlank(detectedMimeType.getName())) {
            return false;
        }

        String detectedMimeTypeName = normalize(detectedMimeType.getName());
        String detectedExtension = normalize(FilenameUtils.getExtension(detectedMimeType.getExtension()));
        if (!isDetectedMimeTypeAllowed(originalExtension, detectedMimeTypeName)) {
            return false;
        }
        // 严格模式要求客户端声明的后缀与真实文件头解析出的规范后缀一致。
        if (!isDetectedExtensionCompatible(originalExtension, detectedExtension, detectedMimeTypeName)) {
            return false;
        }
        return checkContent(originalExtension, uploadFile);
    }

    private boolean validateByFilename(UploadFile uploadFile, String originalExtension) {
        if (!mimeTypeSet.isEmpty()) {
            String mimeType = normalize(MimetypeUtil.detectMimeType(uploadFile.getOriginalFilename()));
            if (!mimeTypeSet.contains(mimeType)) {
                return false;
            }
        }
        return checkContent(originalExtension, uploadFile);
    }

    private boolean checkContent(String extension, UploadFile uploadFile) {
        if (!contentCheckStrategy.hasProvider(extension)) {
            return true;
        }
        return contentCheckStrategy.check(extension, uploadFile);
    }

    private boolean isDetectedMimeTypeAllowed(String originalExtension, String detectedMimeType) {
        if (mimeTypeSet.isEmpty() || mimeTypeSet.contains(detectedMimeType)) {
            return true;
        }
        // 旧版 Word OLE 文档有时只能被 Tika 识别为通用 Microsoft Office 容器。
        return isLegacyMicrosoftWordContainer(originalExtension, detectedMimeType)
                && mimeTypeSet.contains(MICROSOFT_WORD_MIME_TYPE);
    }

    private boolean isDetectedExtensionCompatible(String originalExtension, String detectedExtension,
            String detectedMimeType) {
        if (extensionSet.isEmpty()) {
            return true;
        }
        return originalExtension.equals(detectedExtension)
                || isLegacyMicrosoftWordContainer(originalExtension, detectedMimeType);
    }

    private boolean isLegacyMicrosoftWordContainer(String originalExtension, String detectedMimeType) {
        return "doc".equals(originalExtension)
                && GENERIC_MICROSOFT_OFFICE_MIME_TYPE.equals(detectedMimeType);
    }

    private static Set<String> normalizeSet(String[] values) {
        if (ArrayUtils.isEmpty(values)) {
            return Collections.emptySet();
        }
        Set<String> normalized = Stream.of(values)
                .map(FileValidationEngine::normalize)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toCollection(HashSet::new));
        return Collections.unmodifiableSet(normalized);
    }

    private static String normalize(String value) {
        if (StringUtils.isBlank(value)) {
            return "";
        }
        String normalized = StringUtils.trim(value).toLowerCase(Locale.ROOT);
        return normalized.startsWith(".") ? normalized.substring(1) : normalized;
    }
}
