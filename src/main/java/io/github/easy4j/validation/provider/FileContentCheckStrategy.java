package io.github.easy4j.validation.provider;

import io.github.easy4j.validation.file.UploadFile;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * 按文件扩展名选择内容检查提供者。
 */
public final class FileContentCheckStrategy {

    private static final String WILDCARD = "*/*";

    private final Map<String, FileContentCheckProvider> providers;

    public FileContentCheckStrategy(List<FileContentCheckProvider> providers) {
        Map<String, FileContentCheckProvider> mappings = new LinkedHashMap<String, FileContentCheckProvider>();
        if (Objects.nonNull(providers)) {
            for (FileContentCheckProvider provider : providers) {
                if (Objects.nonNull(provider) && StringUtils.isNotBlank(provider.support())) {
                    mappings.put(normalize(provider.support()), provider);
                }
            }
        }
        this.providers = Collections.unmodifiableMap(mappings);
    }

    public boolean check(String extension, UploadFile uploadFile) {
        FileContentCheckProvider provider = providers.get(normalize(extension));
        if (Objects.isNull(provider)) {
            provider = providers.get(WILDCARD);
        }
        return Objects.isNull(provider) || Boolean.TRUE.equals(provider.check(uploadFile));
    }

    private String normalize(String value) {
        if (StringUtils.isBlank(value)) {
            return "";
        }
        String normalized = StringUtils.trim(value).toLowerCase(Locale.ROOT);
        return normalized.startsWith(".") ? normalized.substring(1) : normalized;
    }
}
