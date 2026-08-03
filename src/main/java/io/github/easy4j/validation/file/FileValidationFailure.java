package io.github.easy4j.validation.file;

/**
 * 文件安全校验失败类型。
 */
public enum FileValidationFailure {
    EMPTY,
    SIZE_EXCEEDED,
    EXTENSION_NOT_ALLOWED,
    MIME_TYPE_NOT_ALLOWED,
    SIGNATURE_MISMATCH,
    CONTENT_REJECTED,
    READ_ERROR
}
