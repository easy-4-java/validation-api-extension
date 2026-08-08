package io.github.easy4j.validation.file;

import java.io.IOException;
import java.io.InputStream;

/**
 * Framework-agnostic functional interface that can repeatedly open a new {@link InputStream}
 * for the uploaded file content.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see DefaultUploadFile
 */
@FunctionalInterface
public interface InputStreamSource {

    /**
     * 打开新的文件内容流。
     *
     * @return 新输入流
     * @throws IOException 内容无法读取时抛出
     */
    InputStream openStream() throws IOException;
}
