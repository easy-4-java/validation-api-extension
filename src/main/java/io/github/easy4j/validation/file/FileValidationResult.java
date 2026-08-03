package io.github.easy4j.validation.file;

/**
 * 文件安全校验结果。
 */
public final class FileValidationResult {

    private final boolean valid;
    private final FileValidationFailure failure;
    private final DetectedFileType detectedFileType;

    private FileValidationResult(boolean valid, FileValidationFailure failure,
            DetectedFileType detectedFileType) {
        this.valid = valid;
        this.failure = failure;
        this.detectedFileType = detectedFileType;
    }

    public static FileValidationResult valid(DetectedFileType detectedFileType) {
        return new FileValidationResult(true, null, detectedFileType);
    }

    public static FileValidationResult invalid(FileValidationFailure failure,
            DetectedFileType detectedFileType) {
        return new FileValidationResult(false, failure, detectedFileType);
    }

    public boolean isValid() {
        return valid;
    }

    public FileValidationFailure getFailure() {
        return failure;
    }

    public DetectedFileType getDetectedFileType() {
        return detectedFileType;
    }
}
