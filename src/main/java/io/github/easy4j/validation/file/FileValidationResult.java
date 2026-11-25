package io.github.easy4j.validation.file;

import lombok.Value;

/** 单次校验结果；成功时 failure 为空，失败时保留已经识别的内容类型。 */
@Value
public class FileValidationResult {
    boolean valid;
    FileValidationFailure failure;
    DetectedFileType detectedType;

    /**
     * @param type 已检测类型，可为空
     * @return 成功结果
     */
    public static FileValidationResult valid(DetectedFileType type) {
        return new FileValidationResult(true, null, type);
    }

    /**
     * @param failure 失败原因
     * @param type 已检测类型，可为空
     * @return 失败结果
     */
    public static FileValidationResult invalid(FileValidationFailure failure, DetectedFileType type) {
        return new FileValidationResult(false, failure, type);
    }
}
