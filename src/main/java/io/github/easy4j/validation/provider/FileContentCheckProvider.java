package io.github.easy4j.validation.provider;

import io.github.easy4j.validation.file.UploadFile;

/**
 * SPI interface for performing extension-specific content checks on uploaded files after
 * basic format validation (size, extension, MIME type) has passed.
 *
 * <p>Implementations are discovered via {@link java.util.ServiceLoader} and registered
 * with {@link FileContentCheckStrategy}.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see FileContentCheckStrategy
 */
public interface FileContentCheckProvider {

    /**
     * Checks whether the content of the given upload file is valid.
     *
     * @param uploadFile the uploaded file to inspect
     * @return {@code true} if the content passes the check
     */
    Boolean check(UploadFile uploadFile);

    /**
     * Returns the file extension (without a leading dot) that this provider handles,
     * for example {@code "txt"}, {@code "doc"}, or {@code "pdf"}.  A wildcard value
     * of the form {@code "*&#47;*"} acts as a catch-all fallback.
     *
     * @return the supported file extension or wildcard
     */
    String support();
}
