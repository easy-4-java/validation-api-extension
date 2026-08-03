package io.github.easy4j.validation.utils;

import jakarta.activation.MimetypesFileTypeMap;
import java.io.File;
import java.util.Objects;

/**
 * 根据文件名或本地文件声明推断 MIME 类型。
 */
public final class MimetypeUtil {

    private static final MimetypesFileTypeMap FILE_TYPE_MAP = new MimetypesFileTypeMap();

    private MimetypeUtil() {
    }

    public static String detectMimeType(File file) {
        return Objects.isNull(file) || !file.exists() ? null : FILE_TYPE_MAP.getContentType(file);
    }

    public static String detectMimeType(String name) {
        return Objects.isNull(name) ? null : FILE_TYPE_MAP.getContentType(name);
    }
}
