package io.github.easy4j.validation.provider;

import io.github.easy4j.validation.file.UploadFile;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/**
 * Strategy that selects the appropriate {@link FileContentCheckProvider} based on the file
 * extension and delegates content checks to it.
 *
 * <p>Providers are keyed by their {@link FileContentCheckProvider#support()} extension
 * (normalized to lower-case, without a leading dot).  A wildcard provider with support
 * A wildcard provider of the form {@code "*&#47;*"} acts as a catch-all fallback.  Duplicate extensions are rejected at
 * construction time.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see FileContentCheckProvider
 */
public final class FileContentCheckStrategy {

    private static final String WILDCARD = "*/*";

    private final Map<String, FileContentCheckProvider> providers;

    /**
     * Creates a new strategy backed by the given providers.
     *
     * @param providers the content check providers (may be {@code null} or empty)
     * @throws IllegalArgumentException if two providers declare the same extension
     */
    public FileContentCheckStrategy(List<FileContentCheckProvider> providers) {
        Map<String, FileContentCheckProvider> mappings = new LinkedHashMap<String, FileContentCheckProvider>();
        if (Objects.nonNull(providers)) {
            List<FileContentCheckProvider> sortedProviders = new ArrayList<FileContentCheckProvider>(providers);
            Collections.sort(sortedProviders, new Comparator<FileContentCheckProvider>() {
                @Override
                public int compare(FileContentCheckProvider left, FileContentCheckProvider right) {
                    return left.getClass().getName().compareTo(right.getClass().getName());
                }
            });
            for (FileContentCheckProvider provider : sortedProviders) {
                if (Objects.nonNull(provider) && StringUtils.isNotBlank(provider.support())) {
                    String support = normalize(provider.support());
                    FileContentCheckProvider existing = mappings.get(support);
                    if (Objects.nonNull(existing)) {
                        throw new IllegalArgumentException("Duplicate file content check providers for support: " + support);
                    }
                    if (Objects.isNull(existing)) {
                        mappings.put(support, provider);
                    }
                }
            }
        }
        this.providers = Collections.unmodifiableMap(mappings);
    }

    /**
     * 加载当前线程上下文类加载器可见的 SPI 内容检查提供者。
     *
     * @return 内容检查策略
     */
    public static FileContentCheckStrategy load() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        ServiceLoader<FileContentCheckProvider> loader = Objects.nonNull(classLoader)
                ? ServiceLoader.load(FileContentCheckProvider.class, classLoader)
                : ServiceLoader.load(FileContentCheckProvider.class);
        List<FileContentCheckProvider> providers = new ArrayList<FileContentCheckProvider>();
        try {
            for (FileContentCheckProvider provider : loader) {
                providers.add(provider);
            }
        } catch (ServiceConfigurationError error) {
            throw new IllegalStateException("Unable to load file content check providers", error);
        }
        return new FileContentCheckStrategy(providers);
    }

    /**
     * 判断指定扩展名是否存在内容检查提供者或通配提供者。
     *
     * @param extension 文件扩展名
     * @return 是否存在可用提供者
     */
    public boolean hasProvider(String extension) {
        return Objects.nonNull(findProvider(extension));
    }

    /**
     * Delegates the content check to the provider matching the given extension (or the
     * wildcard fallback).
     *
     * @param extension the file extension (without a leading dot)
     * @param uploadFile the uploaded file to check
     * @return {@code true} if the content passes the check, or no provider matches
     */
    public boolean check(String extension, UploadFile uploadFile) {
        FileContentCheckProvider provider = findProvider(extension);
        return Objects.nonNull(provider) && Boolean.TRUE.equals(provider.check(uploadFile));
    }

    private FileContentCheckProvider findProvider(String extension) {
        FileContentCheckProvider provider = providers.get(normalize(extension));
        return Objects.nonNull(provider) ? provider : providers.get(WILDCARD);
    }

    private String normalize(String value) {
        if (StringUtils.isBlank(value)) {
            return "";
        }
        String normalized = StringUtils.trim(value).toLowerCase(Locale.ROOT);
        return normalized.startsWith(".") ? normalized.substring(1) : normalized;
    }
}
