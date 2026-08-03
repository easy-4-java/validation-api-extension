package io.github.easy4j.validation.file;

import io.github.easy4j.validation.provider.FileContentCheckProvider;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * 基于 Apache Tika 的框架无关文件安全校验服务。
 */
public final class FileValidationService {

    private final FileTypeDetector fileTypeDetector;
    private final List<FileContentCheckProvider> contentCheckProviders;

    public FileValidationService() {
        this(new TikaFileTypeDetector(), Collections.<FileContentCheckProvider>emptyList());
    }

    public FileValidationService(FileTypeDetector fileTypeDetector,
            Collection<FileContentCheckProvider> contentCheckProviders) {
        this.fileTypeDetector = Objects.requireNonNull(fileTypeDetector,
                "fileTypeDetector must not be null");
        this.contentCheckProviders = Objects.isNull(contentCheckProviders)
                ? Collections.<FileContentCheckProvider>emptyList()
                : Collections.unmodifiableList(new ArrayList<FileContentCheckProvider>(contentCheckProviders));
    }

    /**
     * 按策略校验文件大小、声明扩展名及真实内容类型。
     *
     * @param file 待校验文件，可以为空
     * @param policy 校验策略
     * @return 校验结果
     */
    public FileValidationResult validate(ValidatableFile file, FileValidationPolicy policy) {
        Objects.requireNonNull(policy, "policy must not be null");
        if (Objects.isNull(file) || file.size() <= 0) {
            return policy.isRequired()
                    ? FileValidationResult.invalid(FileValidationFailure.EMPTY, null)
                    : FileValidationResult.valid(null);
        }
        if (policy.getMaxSizeBytes() > 0 && file.size() > policy.getMaxSizeBytes()) {
            return FileValidationResult.invalid(FileValidationFailure.SIZE_EXCEEDED, null);
        }

        String declaredExtension = extensionOf(file.fileName());
        if (!policy.getAllowedExtensions().isEmpty()
                && !policy.getAllowedExtensions().contains(declaredExtension)) {
            return FileValidationResult.invalid(FileValidationFailure.EXTENSION_NOT_ALLOWED, null);
        }

        try {
            DetectedFileType detectedFileType = fileTypeDetector.detect(file);
            if (!detectedFileType.matchesExtension(declaredExtension)) {
                return FileValidationResult.invalid(FileValidationFailure.SIGNATURE_MISMATCH,
                        detectedFileType);
            }
            if (!policy.getAllowedMimeTypes().isEmpty()
                    && !policy.getAllowedMimeTypes().contains(detectedFileType.getMimeType())) {
                return FileValidationResult.invalid(FileValidationFailure.MIME_TYPE_NOT_ALLOWED,
                        detectedFileType);
            }
            if (!checkContent(file, detectedFileType)) {
                return FileValidationResult.invalid(FileValidationFailure.CONTENT_REJECTED,
                        detectedFileType);
            }
            return FileValidationResult.valid(detectedFileType);
        } catch (IOException | RuntimeException exception) {
            return FileValidationResult.invalid(FileValidationFailure.READ_ERROR, null);
        }
    }

    private boolean checkContent(ValidatableFile file, DetectedFileType detectedFileType)
            throws IOException {
        for (FileContentCheckProvider provider : contentCheckProviders) {
            if (provider.supports(detectedFileType) && !provider.check(file, detectedFileType)) {
                return false;
            }
        }
        return true;
    }

    private String extensionOf(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return "";
        }
        int separatorIndex = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
        int extensionIndex = fileName.lastIndexOf('.');
        if (extensionIndex <= separatorIndex || extensionIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(extensionIndex + 1).toLowerCase(Locale.ROOT);
    }
}
