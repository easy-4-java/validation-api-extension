package io.github.easy4j.validation.file;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Apache Tika 从真实文件内容识别出的类型。
 */
public final class DetectedFileType {

    private final String mimeType;
    private final Set<String> extensions;

    public DetectedFileType(String mimeType, Set<String> extensions) {
        this.mimeType = normalize(mimeType);
        LinkedHashSet<String> normalized = new LinkedHashSet<String>();
        if (Objects.nonNull(extensions)) {
            for (String extension : extensions) {
                if (StringUtils.isNotBlank(extension)) {
                    normalized.add(normalizeExtension(extension));
                }
            }
        }
        this.extensions = Collections.unmodifiableSet(normalized);
    }

    public String getMimeType() {
        return mimeType;
    }

    public Set<String> getExtensions() {
        return extensions;
    }

    /**
     * 判断声明扩展名是否属于 Tika 识别类型。
     *
     * @param extension 声明扩展名
     * @return 是否匹配
     */
    public boolean matchesExtension(String extension) {
        return extensions.contains(normalizeExtension(extension));
    }

    private static String normalizeExtension(String extension) {
        String normalized = normalize(extension);
        return normalized.startsWith(".") ? normalized.substring(1) : normalized;
    }

    private static String normalize(String value) {
        return Objects.isNull(value) ? "" : StringUtils.trim(value).toLowerCase(Locale.ROOT);
    }
}
