# validation-api-extension

独立的 Bean Validation 扩展组件，提供常用约束校验器，以及基于 Apache Tika 的框架无关文件类型校验能力。

## 版本线

| 分支 | Java | Bean Validation API | 组件版本 |
| --- | --- | --- | --- |
| `feature/1.0.x` | 8 | Javax Validation 2.0.1 | `1.0.x.20260630-SNAPSHOT` |
| `feature/2.0.x` | 17 | Javax Validation 2.0.1 | `2.0.x.20260630-SNAPSHOT` |
| `feature/3.0.x` | 21 | Jakarta Validation 3.1.1 | `3.0.x.20260630-SNAPSHOT` |

除 Bean Validation 的 `javax` / `jakarta` 命名空间适配外，三个版本线保持相同的公共 API、实现逻辑、注释和文档。
依赖基线随 JDK 版本线递进，避免将较高 JDK 专用依赖带入低版本运行时：

| 关键依赖 | `1.0.x` | `2.0.x` | `3.0.x` |
| --- | --- | --- | --- |
| Apache Tika | 2.9.4 | 3.3.1 | 3.3.2 |
| libphonenumber | 9.0.34 | 9.0.35 | 9.0.36 |
| Apache Commons IO | 2.18.0 | 2.20.0 | 2.22.0 |
| Hutool Core | 5.8.45 | 5.8.46 | 5.8.47 |
| Hibernate Validator | 6.2.4.Final | 6.2.5.Final | 9.1.3.Final |

`1.0.x` 固定使用 JDK 8 可加载的最高 Apache Tika 版本 `2.9.4`（class major version 52）。
Tika `3.2.2+` 虽修复了已知 XXE 漏洞，但其 class major version 为 55，要求 JDK 11，不能进入 JDK 8 版本线。

```xml
<dependency>
    <groupId>io.github.easy4j</groupId>
    <artifactId>validation-api-extension</artifactId>
    <version>1.0.x.20260630-SNAPSHOT</version>
</dependency>
```

## 文件内容类型校验

组件先校验原始文件后缀，再用 Tika 检测真实文件头和容器结构；严格模式下两者必须一致，
不信任客户端声明的 `Content-Type`。`doc/docx/xls/xlsx` 等容器格式建议应用显式引入标准解析包：

```xml
<dependency>
    <groupId>org.apache.tika</groupId>
    <artifactId>tika-parsers-standard-package</artifactId>
    <!-- 将版本替换为上表当前版本线对应的 Apache Tika 版本。 -->
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

`maxSize` 为单文件上限，等于上限时允许上传。严格模式下 Tika 文件头校验始终执行；如果运行时配置了
匹配扩展名的 `FileContentCheckProvider` 或通配 `*/*` Provider，则会继续执行对应的业务内容检查。

公共模块保留生产使用的 `FileNotEmptyValidator`、`FilesNotEmptyValidator`、
`FileContentCheckStrategy`、`FileContentCheckProvider`、`TikaUtil` 和 `MimetypeUtil`。
Spring MVC、Javalin、Quarkus 仅通过 Java SPI 提供 `UploadFileAdapter`，把各自上传对象转换成
方法语义与 Spring `MultipartFile` 一致的 `UploadFile`，公共模块不依赖具体 Web 框架。
