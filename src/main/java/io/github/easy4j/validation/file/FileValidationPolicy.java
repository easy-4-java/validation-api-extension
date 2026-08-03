package io.github.easy4j.validation.file;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * 不可变的文件安全校验策略。
 */
public final class FileValidationPolicy {

    private final boolean required;
    private final long maxSizeBytes;
    private final Set<String> allowedExtensions;
    private final Set<String> allowedMimeTypes;

    private FileValidationPolicy(Builder builder) {
        this.required = builder.required;
        this.maxSizeBytes = builder.maxSizeBytes;
        this.allowedExtensions = immutableNormalized(builder.allowedExtensions, true);
        this.allowedMimeTypes = immutableNormalized(builder.allowedMimeTypes, false);
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isRequired() {
        return required;
    }

    public long getMaxSizeBytes() {
        return maxSizeBytes;
    }

    public Set<String> getAllowedExtensions() {
        return allowedExtensions;
    }

    public Set<String> getAllowedMimeTypes() {
        return allowedMimeTypes;
    }

    private static Set<String> immutableNormalized(Set<String> values, boolean extension) {
        LinkedHashSet<String> normalized = new LinkedHashSet<String>();
        for (String value : values) {
            if (StringUtils.isNotBlank(value)) {
                String item = StringUtils.trim(value).toLowerCase(Locale.ROOT);
                normalized.add(extension && item.startsWith(".") ? item.substring(1) : item);
            }
        }
        return Collections.unmodifiableSet(normalized);
    }

    /**
     * 文件校验策略构建器。
     */
    public static final class Builder {

        private boolean required = true;
        private long maxSizeBytes = 10L * 1024L * 1024L;
        private final Set<String> allowedExtensions = new LinkedHashSet<String>();
        private final Set<String> allowedMimeTypes = new LinkedHashSet<String>();

        public Builder required(boolean required) {
            this.required = required;
            return this;
        }

        public Builder maxSizeBytes(long maxSizeBytes) {
            if (maxSizeBytes < 0) {
                throw new IllegalArgumentException("maxSizeBytes must be greater than or equal to zero");
            }
            this.maxSizeBytes = maxSizeBytes;
            return this;
        }

        public Builder allowedExtensions(String... extensions) {
            if (Objects.nonNull(extensions)) {
                Collections.addAll(this.allowedExtensions, extensions);
            }
            return this;
        }

        public Builder allowedMimeTypes(String... mimeTypes) {
            if (Objects.nonNull(mimeTypes)) {
                Collections.addAll(this.allowedMimeTypes, mimeTypes);
            }
            return this;
        }

        public FileValidationPolicy build() {
            return new FileValidationPolicy(this);
        }
    }
}
