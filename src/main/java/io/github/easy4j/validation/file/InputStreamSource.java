package io.github.easy4j.validation.file;

import java.io.IOException;
import java.io.InputStream;

/**
 * 可重复打开上传内容流的框架无关函数接口。
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
