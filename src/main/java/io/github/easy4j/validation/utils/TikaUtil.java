package io.github.easy4j.validation.utils;

import io.github.easy4j.validation.file.UploadFile;
import org.apache.tika.Tika;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;

import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Utility for detecting real file MIME types using Apache Tika.
 *
 * <p>Unlike filename-based detection, Tika inspects the actual file content (magic bytes)
 * to determine the true MIME type, which helps guard against spoofed file extensions.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see MimetypeUtil
 */
public final class TikaUtil {

    private static final int DETECTION_PREFIX_BYTES = 64 * 1024;
    private static final MimeTypes DEFAULT_MIME_TYPES = MimeTypes.getDefaultMimeTypes();
    private static final Tika TIKA = new Tika();

    private TikaUtil() {
    }

    /**
     * Detects the MIME type of a local file.
     *
     * @param file the file to inspect (may be {@code null} or non-existent)
     * @return the detected MIME type, or {@code null} if the file is absent
     * @throws IOException if detection fails
     */
    public static MimeType detectMimeType(File file) throws IOException {
        if (Objects.isNull(file) || !file.exists()) {
            return null;
        }
        return toMimeType(TIKA.detect(file));
    }

    /**
     * Detects the MIME type from an input stream.
     *
     * <p>For streams supporting {@code mark/reset}, detection inspects at most the first
     * 64 KiB and restores the original position. Formats requiring data beyond that prefix
     * may be reported as a generic container type. For streams without {@code mark/reset},
     * Tika may advance the stream; callers needing to reuse it should supply a markable stream.
     * This method does not close the caller's stream.</p>
     *
     * @param inputStream the input stream (may be {@code null})
     * @return the detected MIME type, or {@code null} if the stream is {@code null}
     * @throws IOException if detection fails
     */
    public static MimeType detectMimeType(InputStream inputStream) throws IOException {
        if (Objects.isNull(inputStream)) {
            return null;
        }
        if (inputStream.markSupported()) {
            inputStream.mark(DETECTION_PREFIX_BYTES);
            try {
                return toMimeType(TIKA.detect(inputStream.readNBytes(DETECTION_PREFIX_BYTES)));
            } finally {
                inputStream.reset();
            }
        }
        return toMimeType(TIKA.detect(new FilterInputStream(inputStream) {
            @Override
            public void close() {
                // The caller owns the supplied stream.
            }
        }));
    }

    /**
     * Detects the MIME type of an uploaded file using both content and filename.
     *
     * @param uploadFile the uploaded file (may be {@code null} or empty)
     * @return the detected MIME type, or {@code null} if the file is absent or empty
     * @throws IOException if detection fails
     */
    public static MimeType detectMimeType(UploadFile uploadFile) throws IOException {
        if (Objects.isNull(uploadFile) || uploadFile.isEmpty()) {
            return null;
        }
        try (InputStream inputStream = uploadFile.getInputStream()) {
            return toMimeType(TIKA.detect(inputStream, uploadFile.getOriginalFilename()));
        }
    }

    /**
     * Detects the MIME type using <em>only</em> the file content (magic bytes), without
     * trusting the client-submitted filename.
     *
     * @param uploadFile the uploaded file (may be {@code null} or empty)
     * @return the detected MIME type, or {@code null} if the file is absent or empty
     * @throws IOException if the content cannot be read or detection fails
     */
    public static MimeType detectContentMimeType(UploadFile uploadFile) throws IOException {
        if (Objects.isNull(uploadFile) || uploadFile.isEmpty()) {
            return null;
        }
        try (InputStream inputStream = uploadFile.getInputStream()) {
            return toMimeType(TIKA.detect(inputStream));
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
