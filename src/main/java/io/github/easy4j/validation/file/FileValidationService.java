package io.github.easy4j.validation.file;

import io.github.easy4j.validation.provider.FileContentCheckStrategy;
import io.github.easy4j.validation.utils.MimetypeUtil;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import static io.github.easy4j.validation.file.FileValidationResult.invalid;
import static io.github.easy4j.validation.file.FileValidationPolicy.normalize;

/** 注解与程序式入口共用的文件规则引擎；校验异常以拒绝结果返回。 */
public final class FileValidationService {
    private final FileTypeDetector detector;
    private final FileContentCheckStrategy contentChecks;
    private final ContentCheck additionalCheck;

    /** 使用 Tika 和当前类加载器的内容检查 SPI。 */
    public FileValidationService() {
        this(new TikaFileTypeDetector(), FileContentCheckStrategy.load(), (file, type) -> true);
    }

    /**
     * 创建显式装配的校验引擎。
     * @param detector 内容类型检测器
     * @param contentChecks 上游内容检查 SPI 策略
     * @param additionalCheck 识别成功后执行的应用规则
     */
    public FileValidationService(FileTypeDetector detector, FileContentCheckStrategy contentChecks,
                                 ContentCheck additionalCheck) {
        this.detector = Objects.requireNonNull(detector, "detector must not be null");
        this.contentChecks = Objects.requireNonNull(contentChecks, "contentChecks must not be null");
        this.additionalCheck = Objects.requireNonNull(additionalCheck, "additionalCheck must not be null");
    }

    /**
     * 校验文件；所有结果仅与本次文件和不可变策略有关。
     * @param file 文件，可为空
     * @param policy 文件策略
     * @return 结构化结果，包含失败原因和已检测类型
     */
    public FileValidationResult validate(UploadFile file, FileValidationPolicy policy) {
        Objects.requireNonNull(policy, "policy must not be null");
        try {
            if (Objects.isNull(file) || file.isEmpty()) {
                return policy.isRequired() ? invalid(FileValidationFailure.EMPTY, null)
                        : FileValidationResult.valid(null);
            }
            if ((policy.isConstraintCompatibility() || policy.getMaxSizeBytes() > 0)
                    && file.getSize() > policy.getMaxSizeBytes()) {
                return invalid(FileValidationFailure.SIZE_EXCEEDED, null);
            }
            boolean compatibility = policy.isConstraintCompatibility();
            boolean unrestricted = policy.getAllowedExtensions().isEmpty() && policy.getAllowedMimeTypes().isEmpty();
            if (compatibility && unrestricted) {
                return checkContent(file, "", null);
            }
            String extension = normalize(FilenameUtils.getExtension(file.getOriginalFilename()), compatibility);
            if (!policy.getAllowedExtensions().isEmpty() && !policy.getAllowedExtensions().contains(extension)) {
                return invalid(FileValidationFailure.EXTENSION_NOT_ALLOWED, null);
            }
            DetectedFileType type = null;
            String mime;
            if (policy.isStrict()) {
                Optional<DetectedFileType> detected = detector.detect(file);
                if (detected.isEmpty()) {
                    return invalid(FileValidationFailure.TYPE_UNDETECTABLE, null);
                }
                type = detected.get();
                mime = normalize(type.getMimeType(), compatibility);
                boolean legacyWord = compatibility && "doc".equals(extension)
                        && "application/x-tika-msoffice".equals(mime);
                if ((!compatibility || !policy.getAllowedExtensions().isEmpty())
                        && !extension.equals(normalize(type.getExtension(), compatibility)) && !legacyWord) {
                    return invalid(FileValidationFailure.SIGNATURE_MISMATCH, type);
                }
                if (legacyWord && policy.getAllowedMimeTypes().contains("application/msword")) {
                    mime = "application/msword";
                }
            } else {
                mime = normalize(compatibility ? MimetypeUtil.detectMimeType(file.getOriginalFilename())
                        : file.getContentType(), compatibility);
            }
            if (!policy.getAllowedMimeTypes().isEmpty() && !policy.getAllowedMimeTypes().contains(mime)) {
                return invalid(FileValidationFailure.MIME_TYPE_NOT_ALLOWED, type);
            }
            return checkContent(file, extension, type);
        } catch (IOException | RuntimeException exception) {
            return invalid(FileValidationFailure.READ_ERROR, null);
        }
    }

    private FileValidationResult checkContent(UploadFile file, String extension, DetectedFileType type)
            throws IOException {
        if (contentChecks.hasProvider(extension) && !contentChecks.check(extension, file)) {
            return invalid(FileValidationFailure.CONTENT_REJECTED, type);
        }
        if (Objects.nonNull(type) && !additionalCheck.check(file, type)) {
            return invalid(FileValidationFailure.CONTENT_REJECTED, type);
        }
        return FileValidationResult.valid(type);
    }

    /** 应用附加内容规则；在基本规则和 SPI 检查通过后执行。 */
    @FunctionalInterface
    public interface ContentCheck {
        /**
         * @param file 上传文件
         * @param type 已检测类型
         * @return 是否接受
         * @throws IOException 无法读取内容
         */
        boolean check(UploadFile file, DetectedFileType type) throws IOException;
    }
}
