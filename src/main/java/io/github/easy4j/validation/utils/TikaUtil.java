package io.github.easy4j.validation.utils;

import io.github.easy4j.validation.file.UploadFile;
import org.apache.tika.Tika;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Apache Tika 文件真实类型检测工具。
 */
public final class TikaUtil {

    private static final MimeTypes DEFAULT_MIME_TYPES = MimeTypes.getDefaultMimeTypes();
    private static final Tika TIKA = new Tika();

    private TikaUtil() {
    }

    public static MimeType detectMimeType(File file) throws IOException {
        if (Objects.isNull(file) || !file.exists()) {
            return null;
        }
        return toMimeType(TIKA.detect(file));
    }

    public static MimeType detectMimeType(InputStream inputStream) throws IOException {
        if (Objects.isNull(inputStream)) {
            return null;
        }
        return toMimeType(TIKA.detect(inputStream));
    }

    public static MimeType detectMimeType(UploadFile uploadFile) throws IOException {
        if (Objects.isNull(uploadFile) || uploadFile.isEmpty()) {
            return null;
        }
        try (InputStream inputStream = uploadFile.getInputStream()) {
            return toMimeType(TIKA.detect(inputStream, uploadFile.getOriginalFilename()));
        }
    }

    private static MimeType toMimeType(String mimeType) throws IOException {
        try {
            return DEFAULT_MIME_TYPES.forName(mimeType);
        } catch (MimeTypeException exception) {
            throw new IOException("Unsupported MIME type: " + mimeType, exception);
        }
    }
}
