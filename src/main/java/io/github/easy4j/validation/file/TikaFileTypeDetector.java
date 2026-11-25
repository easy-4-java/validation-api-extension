package io.github.easy4j.validation.file;

import io.github.easy4j.validation.utils.TikaUtil;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.mime.MimeType;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

/** 使用上游 Tika 内容检测；文件名和声明 MIME 不参与类型推断。 */
public final class TikaFileTypeDetector implements FileTypeDetector {
    @Override
    public Optional<DetectedFileType> detect(UploadFile file) throws IOException {
        MimeType type = TikaUtil.detectContentMimeType(file);
        if (Objects.isNull(type) || StringUtils.isBlank(type.getName())) {
            return Optional.empty();
        }
        return Optional.of(new DetectedFileType(FilenameUtils.getExtension(type.getExtension()), type.getName()));
    }
}
