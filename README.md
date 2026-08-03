# validation-api-extension

独立的 Bean Validation 扩展组件，提供常用约束校验器，以及基于 Apache Tika 的框架无关文件类型校验能力。

## 版本线

| 分支 | Java | Bean Validation API | 组件版本 |
| --- | --- | --- | --- |
| `feature/1.0.x` | 8 | Javax Validation 2.0.1 | `1.0.x.20260630-SNAPSHOT` |
| `feature/2.0.x` | 17 | Javax Validation 2.0.1 | `2.0.x.20260630-SNAPSHOT` |
| `feature/3.0.x` | 17 | Jakarta Validation 3.1.1 | `3.0.x.20260630-SNAPSHOT` |

```xml
<dependency>
    <groupId>io.github.easy4j</groupId>
    <artifactId>validation-api-extension</artifactId>
    <version>1.0.x.20260630-SNAPSHOT</version>
</dependency>
```

## 文件内容类型校验

组件使用 Tika 检测真实文件头及容器结构，不信任客户端文件名和 `Content-Type`。`doc/docx/xls/xlsx`
等容器格式还需要应用显式引入标准解析包：

```xml
<dependency>
    <groupId>org.apache.tika</groupId>
    <artifactId>tika-parsers-standard-package</artifactId>
    <version>2.9.4</version>
</dependency>
```

```java
public class UploadCommand {

    @FileNotEmpty(
            extensions = {"doc", "docx", "xls", "xlsx", "pdf"},
            mimeTypes = {
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "application/vnd.ms-excel",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "application/pdf"
            },
            maxSize = "10MB",
            strict = true)
    private UploadFile file;
}
```

公共模块保留生产使用的 `FileNotEmptyValidator`、`FilesNotEmptyValidator`、
`FileContentCheckStrategy`、`FileContentCheckProvider`、`TikaUtil` 和 `MimetypeUtil`。
Spring MVC、Javalin、Quarkus 仅通过 Java SPI 提供 `UploadFileAdapter`，把各自上传对象转换成
方法语义与 Spring `MultipartFile` 一致的 `UploadFile`，公共模块不依赖具体 Web 框架。
