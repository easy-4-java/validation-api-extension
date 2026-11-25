package io.github.easy4j.validation.file;

import lombok.Value;

/** 内容检测得到的规范扩展名和 MIME；不包含客户端声明值。 */
@Value
public class DetectedFileType {
    String extension;
    String mimeType;
}
