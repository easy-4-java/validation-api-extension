package io.github.easy4j.validation.file;

/**
 * SPI interface for adapting framework-specific upload objects (e.g. Spring
 * {@code MultipartFile}) to the framework-agnostic {@link UploadFile} contract.
 *
 * <p>Implementations are discovered via {@link java.util.ServiceLoader} and registered
 * with {@link UploadFileAdapters}.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see UploadFileAdapters
 */
public interface UploadFileAdapter {

    /**
     * Returns whether this adapter can handle the given value object.
     *
     * @param value the raw upload object to check
     * @return {@code true} if {@link #adapt(Object)} can convert this value
     */
    boolean supports(Object value);

    /**
     * Converts the given framework-specific upload object to an {@link UploadFile}.
     *
     * @param value the raw upload object (never {@code null} when called after {@code supports})
     * @return the adapted upload file
     */
    UploadFile adapt(Object value);
}
