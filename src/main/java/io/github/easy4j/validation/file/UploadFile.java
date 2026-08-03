package io.github.easy4j.validation.file;

import java.io.IOException;
import java.io.InputStream;

/**
 * 框架无关的上传文件接口，方法语义与 Spring MultipartFile 保持一致。
 */
public interface UploadFile {

    String getName();

    String getOriginalFilename();

    String getContentType();

    boolean isEmpty();

    long getSize();

    byte[] getBytes() throws IOException;

    InputStream getInputStream() throws IOException;
}
