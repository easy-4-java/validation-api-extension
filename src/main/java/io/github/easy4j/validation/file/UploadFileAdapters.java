package io.github.easy4j.validation.file;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;

/**
 * 通过 Java SPI 查找上传文件适配器。
 */
public final class UploadFileAdapters {

    private static final List<UploadFileAdapter> ADAPTERS = loadAdapters();

    private UploadFileAdapters() {
    }

    public static UploadFile adapt(Object value) {
        if (value instanceof UploadFile) {
            return (UploadFile) value;
        }
        for (UploadFileAdapter adapter : ADAPTERS) {
            if (adapter.supports(value)) {
                return adapter.adapt(value);
            }
        }
        return null;
    }

    private static List<UploadFileAdapter> loadAdapters() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        ServiceLoader<UploadFileAdapter> loader = Objects.nonNull(classLoader)
                ? ServiceLoader.load(UploadFileAdapter.class, classLoader)
                : ServiceLoader.load(UploadFileAdapter.class);
        List<UploadFileAdapter> adapters = new ArrayList<UploadFileAdapter>();
        for (UploadFileAdapter adapter : loader) {
            adapters.add(adapter);
        }
        return Collections.unmodifiableList(adapters);
    }
}
