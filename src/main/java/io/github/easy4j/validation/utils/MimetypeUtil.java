package io.github.easy4j.validation.utils;

import jakarta.activation.MimetypesFileTypeMap;
import java.io.File;
import java.util.Objects;

/**
 * Utility for detecting MIME types from file names or local files using the Jakarta
 * Activation {@link MimetypesFileTypeMap}.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see TikaUtil
 */
public final class MimetypeUtil {

    private static final MimetypesFileTypeMap FILE_TYPE_MAP = new MimetypesFileTypeMap();

    private MimetypeUtil() {
    }

    /**
     * Detects the MIME type of a local file.
     *
     * @param file the file to inspect (may be {@code null} or non-existent)
     * @return the MIME type string, or {@code null} if the file is {@code null} or does not exist
     */
    public static String detectMimeType(File file) {
        return Objects.isNull(file) || !file.exists() ? null : FILE_TYPE_MAP.getContentType(file);
    }

    /**
     * Detects the MIME type from a file name string.
     *
     * @param name the file name (may be {@code null})
     * @return the MIME type string, or {@code null} if the name is {@code null}
     */
    public static String detectMimeType(String name) {
        return Objects.isNull(name) ? null : FILE_TYPE_MAP.getContentType(name);
    }
}
