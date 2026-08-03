package io.github.easy4j.validation.file;

import java.io.IOException;

/**
 * 根据真实内容识别文件类型的扩展接口。
 */
public interface FileTypeDetector {

    /**
     * 识别文件真实类型。
     *
     * @param file 待识别文件
     * @return 检测结果
     * @throws IOException 内容无法读取或类型无法解析时抛出
     */
    DetectedFileType detect(ValidatableFile file) throws IOException;
}
