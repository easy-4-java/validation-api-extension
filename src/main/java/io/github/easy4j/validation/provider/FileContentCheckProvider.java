package io.github.easy4j.validation.provider;

import io.github.easy4j.validation.file.DetectedFileType;
import io.github.easy4j.validation.file.ValidatableFile;

import java.io.IOException;

/**
 * 文件类型校验通过后执行的可扩展内容安全检查。
 */
public interface FileContentCheckProvider {

    /**
     * 判断是否支持当前检测类型。
     *
     * @param detectedFileType Tika 检测类型
     * @return 是否支持
     */
    boolean supports(DetectedFileType detectedFileType);

    /**
     * 检查文件内容，例如病毒、宏或业务敏感内容。
     *
     * @param file 待检查文件
     * @param detectedFileType Tika 检测类型
     * @return 是否通过
     * @throws IOException 内容无法读取时抛出
     */
    boolean check(ValidatableFile file, DetectedFileType detectedFileType) throws IOException;
}
