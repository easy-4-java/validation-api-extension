package io.github.easy4j.validation.file;

import java.io.IOException;
import java.io.InputStream;

/**
 * Framework-agnostic interface representing an uploaded file, with method semantics
 * aligned to Spring's {@code MultipartFile}.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see DefaultUploadFile
 * @see UploadFileAdapter
 */
public interface UploadFile {

    /**
     * Returns the parameter name of the multipart form field.
     *
     * @return the form field name
     */
    String getName();

    /**
     * Returns the original filename as reported by the client.
     *
     * @return the original filename
     */
    String getOriginalFilename();

    /**
     * Returns the content type (MIME type) declared by the client.
     *
     * @return the content type, or {@code null} if unknown
     */
    String getContentType();

    /**
     * Returns whether the uploaded file is empty (size &le; 0).
     *
     * @return {@code true} if the file is empty
     */
    boolean isEmpty();

    /**
     * Returns the size of the uploaded file in bytes.
     *
     * @return the file size in bytes
     */
    long getSize();

    /**
     * Returns the entire file content as a byte array.
     *
     * @return the file bytes
     * @throws IOException if the content cannot be read
     */
    byte[] getBytes() throws IOException;

    /**
     * Opens a new {@link InputStream} to read the file content.
     *
     * @return a new input stream
     * @throws IOException if the stream cannot be opened
     */
    InputStream getInputStream() throws IOException;
}
