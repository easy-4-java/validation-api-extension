# validation-api-extension

独立的 Bean Validation 扩展组件，提供常用约束校验器，以及基于 Apache Tika 的框架无关文件类型校验能力。

## 版本线

| 分支 | Java | Bean Validation API | 组件版本 |
| --- | --- | --- | --- |
| `feature/1.0.x` | 8 | Javax Validation 2.0.1 | `1.0.x.20260630-SNAPSHOT` |
| `feature/2.0.x` | 17 | Javax Validation 2.0.1 | `2.0.x.20260630-SNAPSHOT` |
| `feature/3.0.x` | 21 | Jakarta Validation 3.1.1 | `3.0.x.20260630-SNAPSHOT` |

```xml
<dependency>
    <groupId>io.github.easy4j</groupId>
    <artifactId>validation-api-extension</artifactId>
    <version>2.0.x.20260630-SNAPSHOT</version>
</dependency>
```

## 文件内容类型校验

组件使用 Tika 检测真实文件头及容器结构，不信任客户端文件名和 `Content-Type`。`doc/docx/xls/xlsx`
等容器格式还需要应用显式引入标准解析包：

```xml
<dependency>
    <groupId>org.apache.tika</groupId>
    <artifactId>tika-parsers-standard-package</artifactId>
    <version>3.3.1</version>
</dependency>
```

```java
FileValidationPolicy policy = FileValidationPolicy.builder()
        .allowedExtensions("doc", "docx", "xls", "xlsx", "pdf")
        .allowedMimeTypes(
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/pdf")
        .maxSizeBytes(10L * 1024L * 1024L)
        .build();

ValidatableFile file = new DefaultValidatableFile(
        fileName, clientContentType, size, inputStreamSource);
FileValidationResult result = new FileValidationService().validate(file, policy);
```

Spring MVC、Javalin 和 Quarkus 只负责把各自的上传对象适配成 `ValidatableFile`；公共模块不依赖具体 Web 框架。
