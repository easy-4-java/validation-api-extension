package io.github.easy4j.validation.file;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/**
 * 通过 Java SPI 查找上传文件适配器。
 */
public final class UploadFileAdapters {

    private static volatile List<UploadFileAdapter> adapters = loadAdapters();

    private UploadFileAdapters() {
    }

    public static UploadFile adapt(Object value) {
        if (value instanceof UploadFile) {
            return (UploadFile) value;
        }
        UploadFileAdapter selected = null;
        for (UploadFileAdapter adapter : adapters) {
            if (adapter.supports(value)) {
                if (Objects.nonNull(selected)) {
                    throw new IllegalStateException("Ambiguous upload file adapters: "
                            + selected.getClass().getName() + " and " + adapter.getClass().getName());
                }
                selected = adapter;
            }
        }
        return Objects.nonNull(selected) ? selected.adapt(value) : null;
    }

    /**
     * 按当前线程上下文类加载器重新加载 SPI 适配器。
     *
     * <p>适用于插件热加载或测试替换类加载器后的显式刷新。</p>
     */
    public static synchronized void reload() {
        adapters = loadAdapters();
    }

    private static List<UploadFileAdapter> loadAdapters() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        ServiceLoader<UploadFileAdapter> loader = Objects.nonNull(classLoader)
                ? ServiceLoader.load(UploadFileAdapter.class, classLoader)
                : ServiceLoader.load(UploadFileAdapter.class);
        List<UploadFileAdapter> adapters = new ArrayList<UploadFileAdapter>();
        try {
            for (UploadFileAdapter adapter : loader) {
                adapters.add(adapter);
            }
        } catch (ServiceConfigurationError error) {
            throw new IllegalStateException("Unable to load upload file adapters", error);
        }
        adapters.sort((left, right) -> left.getClass().getName().compareTo(right.getClass().getName()));
        return Collections.unmodifiableList(adapters);
    }
}
