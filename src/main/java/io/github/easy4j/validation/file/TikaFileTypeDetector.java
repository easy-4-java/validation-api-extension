package io.github.easy4j.validation.file;

import org.apache.tika.Tika;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 使用 Apache Tika 根据文件头和容器结构识别真实文件类型。
 *
 * <p>检测时不会传入客户端文件名，避免扩展名干扰内容类型判断。</p>
 */
public final class TikaFileTypeDetector implements FileTypeDetector {

    private final Tika tika;
    private final MimeTypes mimeTypes;

    public TikaFileTypeDetector() {
        this(defaultTika(), MimeTypes.getDefaultMimeTypes());
    }

    public TikaFileTypeDetector(Tika tika, MimeTypes mimeTypes) {
        this.tika = Objects.requireNonNull(tika, "tika must not be null");
        this.mimeTypes = Objects.requireNonNull(mimeTypes, "mimeTypes must not be null");
    }

    @Override
    public DetectedFileType detect(ValidatableFile file) throws IOException {
        Objects.requireNonNull(file, "file must not be null");
        try (TikaInputStream inputStream = TikaInputStream.get(file.openStream())) {
            String detectedMimeType = tika.detect(inputStream);
            MimeType mimeType = mimeTypes.forName(detectedMimeType);
            Set<String> extensions = new LinkedHashSet<String>(mimeType.getExtensions());
            return new DetectedFileType(mimeType.getName(), extensions);
        } catch (MimeTypeException exception) {
            throw new IOException("Unable to resolve MIME type detected by Apache Tika", exception);
        }
    }

    private static Tika defaultTika() {
        MimeTypes defaultMimeTypes = MimeTypes.getDefaultMimeTypes();
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader classLoader = Objects.nonNull(contextClassLoader)
                ? contextClassLoader
                : TikaFileTypeDetector.class.getClassLoader();
        return new Tika(new DefaultDetector(defaultMimeTypes, classLoader));
    }
}
