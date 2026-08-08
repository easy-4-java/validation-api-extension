package io.github.easy4j.validation.file;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Default, framework-agnostic implementation of {@link UploadFile}.
 *
 * <p>Wraps raw file metadata and an {@link InputStreamSource} to provide the standard
 * upload-file contract without depending on any specific web framework.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see UploadFile
 * @see InputStreamSource
 */
public final class DefaultUploadFile implements UploadFile {

    private final String name;
    private final String originalFilename;
    private final String contentType;
    private final long size;
    private final InputStreamSource inputStreamSource;

    public DefaultUploadFile(String name, String originalFilename, String contentType, long size,
            InputStreamSource inputStreamSource) {
        this.name = name;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.size = size;
        this.inputStreamSource = Objects.requireNonNull(inputStreamSource,
                "inputStreamSource must not be null");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getOriginalFilename() {
        return originalFilename;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean isEmpty() {
        return size <= 0;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public byte[] getBytes() throws IOException {
        try (InputStream inputStream = getInputStream();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = inputStream.read(buffer)) >= 0) {
                outputStream.write(buffer, 0, length);
            }
            return outputStream.toByteArray();
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return inputStreamSource.openStream();
    }
}
