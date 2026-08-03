package io.github.easy4j.validation.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文件大小文本解析工具。
 */
public final class FileSizeUtil {

    private static final Pattern SIZE_PATTERN = Pattern.compile("^([0-9]+)\\s*(B|KB|MB|GB|TB)?$",
            Pattern.CASE_INSENSITIVE);

    private FileSizeUtil() {
    }

    public static long parse(String value) {
        if (StringUtils.isBlank(value)) {
            return 0L;
        }
        Matcher matcher = SIZE_PATTERN.matcher(StringUtils.trim(value));
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Unsupported file size: " + value);
        }
        long amount = Long.parseLong(matcher.group(1));
        String unit = StringUtils.defaultIfBlank(matcher.group(2), "B").toUpperCase(Locale.ROOT);
        return Math.multiplyExact(amount, multiplier(unit));
    }

    private static long multiplier(String unit) {
        if ("KB".equals(unit)) return 1024L;
        if ("MB".equals(unit)) return 1024L * 1024L;
        if ("GB".equals(unit)) return 1024L * 1024L * 1024L;
        if ("TB".equals(unit)) return 1024L * 1024L * 1024L * 1024L;
        return 1L;
    }
}
