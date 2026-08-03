package io.github.easy4j.validation.provider;

import io.github.easy4j.validation.file.UploadFile;

/**
 * 文件基础格式校验通过后执行的扩展内容检查。
 */
public interface FileContentCheckProvider {

    /**
     * 检查文件内容是否合法。
     *
     * @param uploadFile 上传文件
     * @return 是否通过
     */
    Boolean check(UploadFile uploadFile);

    /**
     * 支持的文件扩展名，例如 txt、doc、pdf；星号通配类型表示兜底。
     *
     * @return 支持类型
     */
    String support();
}
