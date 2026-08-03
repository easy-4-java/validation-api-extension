package io.github.easy4j.validation.file;

import java.io.IOException;
import java.io.InputStream;

/**
 * 框架无关的待校验文件，由各 Web 框架适配自身上传类型。
 */
public interface ValidatableFile {

    /**
     * 获取客户端文件名。
     *
     * @return 文件名
     */
    String fileName();

    /**
     * 获取客户端声明的 Content-Type。
     *
     * @return Content-Type，可以为空
     */
    String contentType();

    /**
     * 获取文件字节数。
     *
     * @return 文件大小
     */
    long size();

    /**
     * 打开新的文件内容流。
     *
     * @return 新输入流
     * @throws IOException 内容无法读取时抛出
     */
    InputStream openStream() throws IOException;
}
