package io.github.easy4j.validation.constraintvalidators;

import io.github.easy4j.validation.constraints.FileNotEmpty;
import io.github.easy4j.validation.file.UploadFile;
import io.github.easy4j.validation.file.UploadFileAdapters;
import io.github.easy4j.validation.provider.FileContentCheckStrategy;
import io.github.easy4j.validation.utils.FileSizeUtil;
import io.github.easy4j.validation.utils.MimetypeUtil;
import io.github.easy4j.validation.utils.TikaUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.mime.MimeType;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * 单文件和多文件约束共享的校验逻辑。
 */
final class FileValidationSupport {

    private final Set<String> extensions;
    private final Set<String> mimeTypes;
    private final long maxSize;
    private final boolean required;
    private final boolean strict;

    FileValidationSupport(FileNotEmpty annotation) {
        this.extensions = normalize(annotation.extensions(), true);
        this.mimeTypes = normalize(annotation.mimeTypes(), false);
        this.maxSize = FileSizeUtil.parse(annotation.maxSize());
        this.required = annotation.required();
        this.strict = annotation.strict();
    }

    boolean isRequired() {
        return required;
    }

    boolean isValid(Object value, FileContentCheckStrategy strategy) {
        UploadFile uploadFile = UploadFileAdapters.adapt(value);
        if (Objects.isNull(uploadFile) || uploadFile.isEmpty()) {
            return !required;
        }
        if (maxSize > 0 && uploadFile.getSize() > maxSize) {
            return false;
        }
        if (extensions.isEmpty() && mimeTypes.isEmpty()) {
            return true;
        }

        String declaredExtension = extensionOf(uploadFile.getOriginalFilename());
        if (!extensions.isEmpty() && !extensions.contains(declaredExtension)) {
            return false;
        }
        try {
            if (strict) {
                return validateStrict(uploadFile, declaredExtension, strategy);
            }
            String contentType = StringUtils.defaultIfBlank(uploadFile.getContentType(),
                    MimetypeUtil.detectMimeType(uploadFile.getOriginalFilename()));
            return (mimeTypes.isEmpty() || mimeTypes.contains(normalize(contentType)))
                    && checkContent(strategy, declaredExtension, uploadFile);
        } catch (Exception exception) {
            return false;
        }
    }

    private boolean validateStrict(UploadFile uploadFile, String declaredExtension,
            FileContentCheckStrategy strategy) throws Exception {
        MimeType detected = TikaUtil.detectMimeType(uploadFile);
        if (Objects.isNull(detected) || StringUtils.isBlank(detected.getName())) {
            return false;
        }
        Set<String> detectedExtensions = normalize(
                detected.getExtensions().toArray(new String[0]), true);
        if (detectedExtensions.isEmpty() || !detectedExtensions.contains(declaredExtension)) {
            return false;
        }
        if (!extensions.isEmpty() && Collections.disjoint(extensions, detectedExtensions)) {
            return false;
        }
        if (!mimeTypes.isEmpty() && !mimeTypes.contains(normalize(detected.getName()))) {
            return false;
        }
        return checkContent(strategy, declaredExtension, uploadFile);
    }

    private boolean checkContent(FileContentCheckStrategy strategy, String extension,
            UploadFile uploadFile) {
        return Objects.isNull(strategy) || strategy.check(extension, uploadFile);
    }

    private static Set<String> normalize(String[] values, boolean extension) {
        LinkedHashSet<String> normalized = new LinkedHashSet<String>();
        if (Objects.nonNull(values)) {
            for (String value : values) {
                if (StringUtils.isNotBlank(value)) {
                    String item = normalize(value);
                    normalized.add(extension && item.startsWith(".") ? item.substring(1) : item);
                }
            }
        }
        return Collections.unmodifiableSet(normalized);
    }

    private static String extensionOf(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return "";
        }
        int pathSeparator = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
        int dot = fileName.lastIndexOf('.');
        return dot <= pathSeparator || dot == fileName.length() - 1
                ? "" : normalize(fileName.substring(dot + 1));
    }

    private static String normalize(String value) {
        return Objects.isNull(value) ? ""
                : StringUtils.trim(value).toLowerCase(Locale.ROOT);
    }
}
