package io.github.easy4j.validation.file;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * 通用待校验文件的默认实现。
 */
public final class DefaultValidatableFile implements ValidatableFile {

    private final String fileName;
    private final String contentType;
    private final long size;
    private final InputStreamSource inputStreamSource;

    public DefaultValidatableFile(String fileName, String contentType, long size,
            InputStreamSource inputStreamSource) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.size = size;
        this.inputStreamSource = Objects.requireNonNull(inputStreamSource,
                "inputStreamSource must not be null");
    }

    @Override
    public String fileName() {
        return fileName;
    }

    @Override
    public String contentType() {
        return contentType;
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public InputStream openStream() throws IOException {
        return inputStreamSource.openStream();
    }
}
