[English](./README.md) | [简体中文](./README.zh-CN.md)

# validation-api-extension

![Java](https://img.shields.io/badge/Java-8-blue)
![License](https://img.shields.io/badge/License-Apache%202.0-blue)

**Javax Validation extensions and common constraint validators** —— 独立的 Bean Validation（JSR-380，`javax.validation`）扩展库，提供开箱即用的约束注解，以及基于 Apache Tika 的框架无关文件上传校验能力。

**导航**

- [1. 项目概述](#1-项目概述)
- [2. 能力与状态](#2-能力与状态)
- [3. 环境要求与兼容性](#3-环境要求与兼容性)
- [4. 架构与模块](#4-架构与模块)
- [5. 安装](#5-安装)
- [6. 快速开始](#6-快速开始)
- [7. 配置](#7-配置)
- [8. 核心用法 / API](#8-核心用法--api)
- [9. 测试与构建](#9-测试与构建)
- [10. 版本线与分支](#10-版本线与分支)
- [11. 贡献与许可](#11-贡献与许可)

## 1. 项目概述

`validation-api-extension` 是 Bean Validation 2.0（`javax.validation`）的纯 Java 扩展库，提供：

- 常用约束注解（`@IdCard`、`@PhoneNumber`、`@Regexp`、`@Contains`、`@NumberValue`、`@StringDateValue`、`@AllowableValues`）及其校验器，底层由正则属性资源与 libphonenumber 支撑。
- 文件上传校验（`@FileNotEmpty` / `@FilesNotEmpty`）：检查文件是否存在、扩展名白名单、大小上限，并在严格模式下通过 Apache Tika 校验真实文件内容（不信任客户端声明的 `Content-Type`）。
- 框架无关的上传模型（`UploadFile`、`UploadFileAdapter`，基于 Java SPI），方法语义与 Spring `MultipartFile` 保持一致。

**它不是什么**

- 不是校验引擎 —— 它插拔到任意 Bean Validation 实现（测试使用 Hibernate Validator）。
- 在 1.0.x 版本线上不是 Jakarta Validation（`jakarta.validation`）构建 —— 3.0.x 版本线切换到 Jakarta 命名空间（见第 10 节）。
- 不绑定 Spring / Javalin / Quarkus —— 核心模块仅依赖 `javax.validation` API。

**典型场景**

| 场景 | 组件的作用 |
|:---|:---|
| 校验中国身份证号 | `@IdCard` |
| 按地区校验手机号 | `@PhoneNumber(lang = "CN")`（libphonenumber） |
| 复用经过验证的正则模式 | `@Regexp` / `@Contains`，底层为 `regexp_*.properties` |
| 数字串 / 日期格式串校验 | `@NumberValue` / `@StringDateValue` |
| 限定字段值白名单 | `@AllowableValues` |
| 校验上传文件（扩展名 + 大小 + MIME + 真实文件头） | `@FileNotEmpty` + Tika 检测 + `FileContentCheckStrategy` |

## 2. 能力与状态

| 能力 | 状态 | 说明 |
|:---|:---|:---|
| `@IdCard` | 稳定 | 中国居民身份证（15/18 位）格式与校验位验证 |
| `@PhoneNumber` | 稳定 | 基于 libphonenumber 的地区感知手机号校验（`lang` 属性，默认 `CN`） |
| `@Regexp` / `@Contains` | 稳定 | 支持 `Perl5Compiler` mask 的正则校验，模式缓存加载自 `regexp_*.properties`（date、html、math、mobile、net、normal、special、sql） |
| `@NumberValue` / `@StringDateValue` | 稳定 | 数字串与日期格式串约束，支持自定义正则 / 格式 |
| `@AllowableValues` | 稳定 | 白名单约束，支持 `nullable` |
| `@FileNotEmpty` / `@FilesNotEmpty` | 稳定 | 上传校验：必填开关、扩展名白名单、大小上限（B/KB/MB/GB/TB）、MIME 白名单、Tika 严格内容校验 |
| 真实内容检测 | 稳定 | `TikaUtil.detectMimeType(...)` 检测真实文件头 / 容器类型，而非客户端声明的类型 |
| SPI 内容检查 | 稳定 | `FileContentCheckProvider` 通过 `ServiceLoader` 发现，按优先级排序，由 `FileContentCheckStrategy` 分发 |
| 框架无关上传模型 | 稳定 | `UploadFile` 接口 + `UploadFileAdapter` SPI；方法语义与 Spring `MultipartFile` 一致 |
| API 兼容门禁 | 稳定 | Clirr `api-compatibility` profile + CI 任务守护公开 API 表面 |

## 3. 环境要求与兼容性

| 要求 | 版本 |
|:---|:---|
| JDK | 8+（`feature/1.0.x` 分支基线，`maven.compiler.release=8`） |
| Maven | 3.0+ |
| Bean Validation API | `javax.validation:validation-api` 2.0.1.Final |
| 运行时实现 | 任意 JSR-380 实现（测试使用 Hibernate Validator 6.2.4.Final） |

**版本线矩阵**

| 分支 | JDK | Bean Validation API | 版本模式 |
|:---|:---|:---|:---|
| `feature/1.0.x` | 8 | Javax Validation 2.0.1 | `1.0.x.*` |
| `feature/2.0.x` | 17 | Javax Validation 2.0.1 | `2.0.x.*` |
| `feature/3.0.x` | 21 | Jakarta Validation 3.1.1 | `3.0.x.*` |

除 `javax` / `jakarta` 命名空间适配外，三条版本线保持相同的公共 API、实现逻辑与文档。依赖基线随版本线递进（已从各分支 POM 核实）：

| 关键依赖 | `1.0.x` | `2.0.x` | `3.0.x` |
|:---|:---|:---|:---|
| Apache Tika | 2.9.4 | 3.3.1 | 3.3.2 |
| libphonenumber | 9.0.34 | 9.0.35 | 9.0.36 |
| Apache Commons IO | 2.22.0 | 2.22.0 | 2.22.0 |
| Hutool Core | 5.8.45 | 5.8.46 | 5.8.47 |
| Hibernate Validator（测试） | 6.2.4.Final | 6.2.5.Final | 9.1.3.Final |

`1.0.x` 版本线将 Apache Tika 固定为 **2.9.4** —— 这是 JDK 8 可加载的最新 Tika 版本（class major version 52）。Tika 3.2.2+ 虽修复了已知 XXE 漏洞，但其 class major version 为 55、要求 JDK 11，无法进入 JDK 8 版本线。

## 4. 架构与模块

```text
   Bean Validation（javax.validation，如 Hibernate Validator）
                          |
   +----------------------------------------------------+
   | @IdCard  @PhoneNumber  @Regexp  @Contains           |
   | @NumberValue  @StringDateValue  @AllowableValues    |
   +----------------------------------------------------+
                          |
                  constraintvalidators 包
   +----------------------------------------------------+
   | @FileNotEmpty / @FilesNotEmpty                     |
   |    -> FileValidationEngine                         |
   |        * 扩展名白名单 / 大小上限                     |
   |        * TikaUtil 真实文件头检测                     |
   |        * FileContentCheckStrategy（SPI）            |
   +----------------------------------------------------+
```

**模块清单**

| 模块 | 类型 | 职责 |
|:---|:---|:---|
| `validation-api-extension` | 单 jar（库） | 约束、校验器、上传模型、Tika 工具、正则属性资源 |

**包结构**（`io.github.easy4j.validation`）

| 包 | 内容 |
|:---|:---|
| `constraints` | `AllowableValues`、`Contains`、`FileNotEmpty`、`IdCard`、`NumberValue`、`PhoneNumber`、`Regexp`、`StringDateValue` |
| `constraintvalidators` | `AllowedValuesValidator`、`ContainsValidator`、`FileNotEmptyValidator`、`FilesNotEmptyValidator`、`FileValidationEngine`、`IdCardValidator`、`NumberValueValidator`、`PhoneValueValidator`、`RegexpValidator`、`StringDateValueValidator` |
| `file` | `UploadFile`、`DefaultUploadFile`、`InputStreamSource`、`UploadFileAdapter`、`UploadFileAdapters`（ServiceLoader 发现） |
| `provider` | `FileContentCheckProvider`、`FileContentCheckStrategy` |
| `utils` | `TikaUtil`、`MimetypeUtil`、`IDCardUtils`、`IdcardUtils2`、`JakartaOROUtils`、`JakartaRegexpUtils`、`PatternMatchUtils`、`RegexpPatternCache`、`RegexpPatternUtils` |
| resources | `regexp_date.properties`、`regexp_html.properties`、`regexp_math.properties`、`regexp_mobile.properties`、`regexp_net.properties`、`regexp_normal.properties`、`regexp_special.properties`、`regexp_sql.properties` |

## 5. 安装

> **假设**：制品目前通过项目私有 Maven 仓库（阿里云）与 GitHub Releases 分发；该库**尚未发布到 Maven Central**。若下列坐标无法解析，请在构建中配置私有仓库，或使用 `./mvnw install` 本地安装。

**Maven**

```xml
<dependency>
    <groupId>io.github.easy4j</groupId>
    <artifactId>validation-api-extension</artifactId>
    <version>1.0.x.20260630-SNAPSHOT</version>
</dependency>
```

**Gradle**

```gradle
implementation 'io.github.easy4j:validation-api-extension:1.0.x.20260630-SNAPSHOT'
```

对于 `doc/docx/xls/xlsx` 等容器格式，建议应用显式引入标准 Tika 解析包（本库中为 provided 作用域）：

```xml
<dependency>
    <groupId>org.apache.tika</groupId>
    <artifactId>tika-parsers-standard-package</artifactId>
    <version>2.9.4</version>
</dependency>
```

## 6. 快速开始

**约束校验**（任意 JSR-380 实现）：

```java
public class UserDto {

    @IdCard
    private String idCard;

    @PhoneNumber(lang = "CN")
    private String phone;

    @NumberValue(message = "must be a numeric string")
    private String age;
}
```

```java
ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
Validator validator = factory.getValidator();

UserDto user = new UserDto();
user.setIdCard("110101199003077756");  // 校验位正确 -> 无违规
user.setPhone("13800138000");
user.setAge("42");

Set<ConstraintViolation<UserDto>> violations = validator.validate(user);
// 预期结果：全部值合法时 violations 为空；
// 非法值会产生携带配置消息的违规项。
```

**文件上传校验**：

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

`maxSize` 为单文件上限，等于上限时允许上传。严格模式下 Tika 文件头校验始终执行；如果运行时注册了匹配扩展名的 `FileContentCheckProvider` 或通配 `*/*` Provider，随后还会执行对应的业务内容检查。

## 7. 配置

这是**纯库 —— 没有配置文件或属性前缀**。唯一的扩展机制是 Java SPI：

- 实现 `UploadFileAdapter`，将所在框架的上传对象（如 Spring `MultipartFile`）转换为 `UploadFile`。
- 实现 `FileContentCheckProvider` 做业务级内容检查；Provider 按优先级排序，由 `FileContentCheckStrategy` 分发（`load()` 引导 `ServiceLoader`）。

将实现注册到 classpath 上的 `META-INF/services/io.github.easy4j.validation.file.UploadFileAdapter`（或对应的 Provider 文件）即可。

## 8. 核心用法 / API

**约束速查**（`io.github.easy4j.validation.constraints`）：

| 注解 | 关键属性 | 底层实现 |
|:---|:---|:---|
| `@IdCard` | — | `IDCardUtils`（格式 + 校验位） |
| `@PhoneNumber` | `lang`（默认 `CN`）、`value`（附加正则） | libphonenumber |
| `@Regexp` | `mask`（默认 `CASE_INSENSITIVE_MASK`） | Jakarta ORO / 正则属性 |
| `@Contains` | `mask` | Jakarta ORO |
| `@NumberValue` | `regex`（默认 `^[0-9\-]+$`） | 正则 |
| `@StringDateValue` | `pattern`（默认 `yyyy-MM-dd`） | 日期解析 |
| `@AllowableValues` | `allows`、`nullable` | 白名单 |

**文件 / 内容工具**：

```java
// 检测真实内容类型（文件头 / 容器），而非客户端声明的类型
MimeType mime = TikaUtil.detectMimeType(uploadFile);   // UploadFile、File 或 InputStream
String mimeType = MimetypeUtil.detectMimeType(file);   // 按文件或名称返回字符串

// 对指定扩展名运行已注册的 SPI 内容 Provider
FileContentCheckStrategy strategy = FileContentCheckStrategy.load();
if (strategy.hasProvider("pdf")) {
    boolean ok = strategy.check("pdf", uploadFile);
}
```

## 9. 测试与构建

```bash
./mvnw clean verify     # 单元测试 + JaCoCo 覆盖率报告
./mvnw -Papi-compatibility -Dapi.compatibility.version=<基线版本> verify   # Clirr 公共 API 检查
```

- **测试**：6 个测试类 / 13 个 `@Test` 方法，覆盖约束、文件校验引擎与 SPI 适配器发现。
- **覆盖率门禁**：JaCoCo 在 `verify` 阶段校验行覆盖率不低于 90%（`haltOnFailure=false`）。
- **CI**（`.github/workflows/build.yml`）：JDK 8 矩阵执行 `mvn -B -ntp clean verify`；PR 上执行依赖审查；另有 API 兼容性任务，用 Clirr 将当前公共 API 与目标分支基线比对。

## 10. 版本线与分支

| 分支 | JDK | Validation API | 版本模式 |
|:---|:---|:---|:---|
| `feature/1.0.x` | 8 | Javax Validation 2.0.1 | `1.0.x.*` |
| `feature/2.0.x` | 17 | Javax Validation 2.0.1 | `2.0.x.*` |
| `feature/3.0.x` | 21 | Jakarta Validation 3.1.1 | `3.0.x.*` |

- 快照版本遵循 `1.0.x.yyyyMMdd-SNAPSHOT` 命名；发布版本以 `v{version}` 打标签，并通过项目私有仓库与 GitHub Releases 分发。
- `1.0.x` 是持续维护的 JDK 8 / Javax 版本线；2.0.x 将 JDK 基线提升至 17 且保持 Javax 命名空间；3.0.x 在 JDK 21 上迁移到 Jakarta Validation 3.x。

## 11. 贡献与许可

欢迎贡献 —— 请在 GitHub 上提交 Issue 或 Pull Request。

本项目基于 **Apache License, Version 2.0** 许可发布。详见 [LICENSE](./LICENSE) 文件。
