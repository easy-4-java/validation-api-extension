package io.github.easy4j.validation.file;

/**
 * 将具体框架的上传对象适配为 {@link UploadFile}。
 */
public interface UploadFileAdapter {

    boolean supports(Object value);

    UploadFile adapt(Object value);
}
