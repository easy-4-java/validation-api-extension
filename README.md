# validation-api-extension

[English](./README.md) | [简体中文](./README.zh-CN.md)

[![Java](https://img.shields.io/badge/Java-8-orange)](https://github.com/easy-4-java/validation-api-extension) [![License](https://img.shields.io/badge/license-Apache%202.0-green)](./LICENSE)

Javax Validation extensions and common constraint validators — a standalone Bean Validation (JSR-380, `javax.validation`) extension library with ready-to-use constraint annotations and framework-agnostic file upload validation backed by Apache Tika.

## Table of Contents

- [1. Project Overview](#1-project-overview)
- [2. Features & Status](#2-features--status)
- [3. Requirements & Compatibility](#3-requirements--compatibility)
- [4. Architecture & Modules](#4-architecture--modules)
- [5. Installation](#5-installation)
- [6. Quick Start](#6-quick-start)
- [7. Configuration](#7-configuration)
- [8. Core Usage / API](#8-core-usage--api)
- [9. Testing & Build](#9-testing--build)
- [10. Versioning & Branches](#10-versioning--branches)
- [11. Contributing & License](#11-contributing--license)

## 1. Project Overview

`validation-api-extension` is a pure-Java extension for Bean Validation 2.0 (`javax.validation`). It provides:

- Common constraint annotations (`@IdCard`, `@PhoneNumber`, `@Regexp`, `@Contains`, `@NumberValue`, `@StringDateValue`, `@AllowableValues`) with their validators, backed by regex property resources and libphonenumber.
- File upload validation (`@FileNotEmpty` / `@FilesNotEmpty`) that checks file presence, extension whitelist, size limit and — in strict mode — the real file content via Apache Tika (do not trust the client-declared `Content-Type`).
- A framework-agnostic upload model (`UploadFile`, `UploadFileAdapter` via Java SPI) whose method semantics match Spring `MultipartFile`.

**What it is not**

- Not a validation engine — it plugs into any Bean Validation provider (Hibernate Validator is used in tests).
- Not a Jakarta Validation (`jakarta.validation`) build on the 1.0.x line — the 2.0.x and 3.0.x lines use Jakarta namespaces (see Section 10).
- Not tied to Spring / Javalin / Quarkus — the core module depends only on the `javax.validation` API.

**Typical scenarios**

| Scenario | How this component helps |
|:---|:---|
| Validate Chinese ID card numbers | `@IdCard` |
| Validate phone numbers per region | `@PhoneNumber(lang = "CN")` (libphonenumber) |
| Reuse well-tested regex patterns | `@Regexp` / `@Contains` backed by `regexp_*.properties` |
| Numeric / date-format string checks | `@NumberValue` / `@StringDateValue` |
| Restrict field values to an allowlist | `@AllowableValues` |
| Validate upload files (extension + size + MIME + real header) | `@FileNotEmpty` + Tika detection + `FileContentCheckStrategy` |

## 2. Features & Status

| Capability | Status | Description |
|:---|:---|:---|
| `@IdCard` | Stable | Chinese resident ID card (15/18 digits) format + checksum validation |
| `@PhoneNumber` | Stable | Region-aware phone validation via libphonenumber (`lang` attribute, default `CN`) |
| `@Regexp` / `@Contains` | Stable | Regex validation with `Perl5Compiler` masks, plus pattern caches loaded from `regexp_*.properties` (date, html, math, mobile, net, normal, special, sql) |
| `@NumberValue` / `@StringDateValue` | Stable | Numeric-string and date-format-string constraints with configurable regex / pattern |
| `@AllowableValues` | Stable | Whitelist constraint with `nullable` support |
| `@FileNotEmpty` / `@FilesNotEmpty` | Stable | Upload validation: required flag, extension whitelist, size limit (B/KB/MB/GB/TB), MIME whitelist, strict Tika content check |
| Real content detection | Stable | `TikaUtil.detectMimeType(...)` detects the actual file header / container, not the client-declared type |
| SPI content checks | Stable | `FileContentCheckProvider` discovered via `ServiceLoader`, ordered by priority, dispatched by `FileContentCheckStrategy` |
| Framework-agnostic upload model | Stable | `UploadFile` interface + `UploadFileAdapter` SPI; method semantics match Spring `MultipartFile` |
| API compatibility check | Optional | Clirr `api-compatibility` profile compares against an explicitly selected baseline |

## 3. Requirements & Compatibility

| Requirement | Version |
|:---|:---|
| JDK | 8+ (`feature/1.0.x`, `maven.compiler.release=1.8`) |
| Maven | Wrapper 3.9.16 (`bash ./mvnw`) |
| Bean Validation API | `javax.validation:validation-api` 2.0.1.Final |
| Runtime provider | Hibernate Validator 6.2.4.Final (test baseline) |

**Version line matrix**

| Branch | JDK | Bean Validation API | Version pattern |
|:---|:---|:---|:---|
| `feature/1.0.x` | 8 | Javax Validation 2.0.1 | `1.0.x.*` |
| `feature/2.0.x` | 17 | Jakarta Validation 3.1.1 | `2.0.x.*` |
| `feature/3.0.x` | 21 | Jakarta Validation 3.1.1 | `3.0.x.*` |

Besides the `javax` / `jakarta` namespace adaptation, the three lines keep the same public API, implementation and documentation. Dependency baselines evolve per line (verified from the branch POMs):

| Key dependency | `1.0.x` | `2.0.x` | `3.0.x` |
|:---|:---|:---|:---|
| Apache Tika | 2.9.4 | 3.3.1 | 3.3.2 |
| libphonenumber | 9.0.39 | 9.0.39 | 9.0.39 |
| Apache Commons IO | 2.22.0 | 2.22.0 | 2.22.0 |
| Hutool Core | 5.8.46 | 5.8.46 | 5.8.47 |
| Hibernate Validator (test) | 6.2.4.Final | 8.0.3.Final | 9.1.3.Final |

The `1.0.x` line retains Tika 2.9.4 for JDK 8. The Tika 4 compatibility profile is available on JDK 17/21 only.

## 4. Architecture & Modules

```text
   Bean Validation (javax.validation, e.g. Hibernate Validator)
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
   |        * extension whitelist / size limit          |
   |        * TikaUtil real-header detection            |
   |        * FileContentCheckStrategy (SPI)            |
   +----------------------------------------------------+
```

**Module list**

| Module | Type | Responsibility |
|:---|:---|:---|
| `validation-api-extension` | Single jar (library) | Constraints, validators, upload model, Tika utilities, regex pattern resources |

**Package layout** (`io.github.easy4j.validation`)

| Package | Content |
|:---|:---|
| `constraints` | `AllowableValues`, `Contains`, `FileNotEmpty`, `IdCard`, `NumberValue`, `PhoneNumber`, `Regexp`, `StringDateValue` |
| `constraintvalidators` | `AllowedValuesValidator`, `ContainsValidator`, `FileNotEmptyValidator`, `FilesNotEmptyValidator`, `FileValidationEngine`, `IdCardValidator`, `NumberValueValidator`, `PhoneValueValidator`, `RegexpValidator`, `StringDateValueValidator` |
| `file` | `UploadFile`, `DefaultUploadFile`, `InputStreamSource`, `UploadFileAdapter`, `UploadFileAdapters` (ServiceLoader discovery) |
| `provider` | `FileContentCheckProvider`, `FileContentCheckStrategy` |
| `utils` | `TikaUtil`, `MimetypeUtil`, `IDCardUtils`, `IdcardUtils2`, `JakartaOROUtils`, `JakartaRegexpUtils`, `PatternMatchUtils`, `RegexpPatternCache`, `RegexpPatternUtils` |
| resources | `regexp_date.properties`, `regexp_html.properties`, `regexp_math.properties`, `regexp_mobile.properties`, `regexp_net.properties`, `regexp_normal.properties`, `regexp_special.properties`, `regexp_sql.properties` |

## 5. Installation

> **Assumption**: artifacts are currently distributed through the project's private Maven repository (Aliyun) and GitHub Releases; the library is **not yet published to Maven Central**. If the coordinates below cannot be resolved, either add the private repository to your build or install locally with `bash ./mvnw install`.

**Maven**

```xml
<dependency>
    <groupId>io.github.easy4j</groupId>
    <artifactId>validation-api-extension</artifactId>
    <version>2.0.x.20260630-SNAPSHOT</version>
</dependency>
```

**Gradle**

```gradle
implementation 'io.github.easy4j:validation-api-extension:1.0.x.20260630-SNAPSHOT'
```

For container formats (`doc/docx/xls/xlsx`, ...), applications are recommended to bring the standard Tika parser package explicitly (provided-scope in this library):

```xml
<dependency>
    <groupId>org.apache.tika</groupId>
    <artifactId>tika-parsers-standard-package</artifactId>
    <version>2.9.4</version>
</dependency>
```

## 6. Quick Start

**Constraint validation** (any JSR-380 provider):

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
user.setIdCard("110101199003077756");  // valid checksum -> no violation
user.setPhone("13800138000");
user.setAge("42");

Set<ConstraintViolation<UserDto>> violations = validator.validate(user);
// Expected result: violations is empty when all values are valid;
// invalid values yield violations with the configured messages.
```

**File upload validation**:

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

`maxSize` is the per-file upper limit and equals-limit is allowed. In strict mode the Tika header check always runs; if a matching-extension `FileContentCheckProvider` or a wildcard `*/*` provider is registered at runtime, the corresponding business content check runs afterwards.

## 7. Configuration

This is a **pure library — there is no configuration file or property prefix**. The only extension mechanism is Java SPI:

- Implement `UploadFileAdapter` to convert your framework's upload object (e.g. Spring `MultipartFile`) into an `UploadFile`.
- Implement `FileContentCheckProvider` for business-level content checks; providers are sorted by priority and dispatched by `FileContentCheckStrategy` (`load()` bootstraps the `ServiceLoader`).

Register implementations in `META-INF/services/io.github.easy4j.validation.file.UploadFileAdapter` (or the provider equivalent) on your classpath.

## 8. Core Usage / API

**Constraint reference** (`io.github.easy4j.validation.constraints`):

| Annotation | Key attributes | Backed by |
|:---|:---|:---|
| `@IdCard` | — | `IDCardUtils` (format + checksum) |
| `@PhoneNumber` | `lang` (default `CN`), `value` (extra regex) | libphonenumber |
| `@Regexp` | `mask` (default `CASE_INSENSITIVE_MASK`) | Jakarta ORO / regexp properties |
| `@Contains` | `mask` | Jakarta ORO |
| `@NumberValue` | `regex` (default `^[0-9\-]+$`) | regex |
| `@StringDateValue` | `pattern` (default `yyyy-MM-dd`) | date parsing |
| `@AllowableValues` | `allows`, `nullable` | allowlist |

**File / content utilities**:

```java
// Detect the REAL content type (header / container), not the client-declared one
MimeType mime = TikaUtil.detectContentMimeType(uploadFile);   // content only; ignores the submitted filename
String mimeType = MimetypeUtil.detectMimeType(file);   // string variant by file or name

// Run registered SPI content providers for a given extension
FileContentCheckStrategy strategy = FileContentCheckStrategy.load();
if (strategy.hasProvider("pdf")) {
    boolean ok = strategy.check("pdf", uploadFile);
}
```

## 9. Testing & Build

```bash
bash ./mvnw clean verify     # unit tests + JaCoCo coverage report
bash ./mvnw -Papi-compatibility -Dapi.compatibility.version=<baseline> verify   # Clirr public-API check
```

- **Tests**: constraints, file rules, SPI, data permissions and full-stream detection; see Surefire reports for executed counts.
- **Coverage gate**: JaCoCo checks a 90% line-coverage minimum at the `verify` phase (`haltOnFailure=true`).
- **CI** (`.github/workflows/ci.yml`): version branches, main and test/* pushes/PRs use the line-specific JDK and Maven Wrapper. JDK 17/21 verify both baseline and Tika 4. Clirr is an opt-in check with an explicit baseline.

## 10. Versioning & Branches

| Branch | JDK | Validation API | Version pattern |
|:---|:---|:---|:---|
| `feature/1.0.x` | 8 | Javax Validation 2.0.1 | `1.0.x.*` |
| `feature/2.0.x` | 17 | Jakarta Validation 3.1.1 | `2.0.x.*` |
| `feature/3.0.x` | 21 | Jakarta Validation 3.1.1 | `3.0.x.*` |

- Snapshot versions follow the `1.0.x.yyyyMMdd-SNAPSHOT` scheme; releases are tagged `v{version}` and published through the project's private repository and GitHub Releases.
- The `1.0.x` line is the actively maintained JDK 8 / Javax line. The 2.0.x line uses JDK 17 and Jakarta namespaces; the 3.0.x line moves to Jakarta Validation 3.x on JDK 21.

## 11. Contributing & License

Contributions are welcome — please open an issue or a pull request on GitHub.

This project is licensed under the **Apache License, Version 2.0**. See the [LICENSE](./LICENSE) file for details.

## Structured file validation

`io.github.easy4j.validation.file.FileValidationService` is the shared validation engine for both programmatic callers and `@FileNotEmpty`. Use `DefaultUploadFile`, `FileValidationPolicy`, `FileValidationResult` and `FileValidationFailure` directly. The default `TikaFileTypeDetector` identifies real content; custom detectors implement `FileTypeDetector`, and content rules retain the existing `FileContentCheckStrategy` / ServiceLoader integration. Annotation compatibility rules remain in the shared service's policy, without maintaining a second validation engine.

This maintenance line keeps its existing Java, Validation API and Tika versions. Changes are verified with the full component suite and DDD4J integration tests.

## Data permission validation

Use `io.github.easy4j.validation.constraints.RequiresDataPermissions`,
`constraintvalidators.RequiresDataPermissionsValidator`, and `provider.DataScopeProvider` directly.
The application implements `hasPermissions(dataType, data)`; there is no permissive default.

Plain Bean Validation must supply the provider through its `ConstraintValidatorFactory`, using the
validator's provider constructor and delegating other constraints to the existing factory.
Spring applications register the provider as a bean and reuse `LocalValidatorFactoryBean` /
`SpringConstraintValidatorFactory`; standard `@Inject` supplies the provider without a custom factory.
Missing or ambiguous providers are configuration errors. With a configured provider, null values
are constraint violations, unauthorized values retain the annotation's groups/message/payload,
and provider exceptions propagate. Providers must read request identity per call rather than cache
request-specific authorization decisions in shared fields. See `DataPermissionsValidationTest` for
executable plain-validation examples.

The former DDD4J data/auth annotations and provider types are removed: update imports and recompile.
There is no `nonNullAllowed()` fallback. This object-level constraint does not rewrite SQL.


## Programmatic validation and DDD4J integration

`io.github.easy4j.validation.file.FileValidationService` owns the shared rules used by both file constraints and programmatic consumers.
Its immutable `FileValidationPolicy` and structured `FileValidationResult` expose failure reasons and detected types.
The annotation adapter selects `constraintCompatibility(true)` to preserve its existing empty-allowlist, filename-MIME,
zero-byte-limit and legacy Word-container behavior. Programmatic defaults require a file, inspect content, enforce the
filename extension, and allow up to 2 MiB; a zero size limit is unbounded in that mode.

The service uses the existing content-check SPI and supports an injected detector plus application-specific checks.
Tika's standard parser package remains `provided`. Office consumers supply a `tika-parser-microsoft-module` matching tika-core.
All three lines expose the public file API and data permission contracts; consumers must use the matching artifact containing these APIs.

## Stream detection contract and Tika 4 compatibility

- `detectMimeType(InputStream)` leaves caller-owned streams open. Byte-array streams retain their starting position; other markable streams up to 1 MiB are fully inspected and reset.
- Larger or non-markable streams receive full detection and may advance; the buffering threshold does not reject them or truncate container inspection. Late JAR/Office metadata is inspected.
- `detectContentMimeType(UploadFile)` ignores the filename and closes the stream it obtains; read failures remain IOExceptions.
- Baselines: 1.0.x / Tika 2.9.4, 2.0.x / Tika 3.3.1, 3.0.x / Tika 3.3.2. On JDK 17/21, run `bash ./mvnw -Ptika4-compatibility clean verify` to verify Tika 4.0.0 and its POM-packaged standard parsers with the same 90% coverage gate. This profile does not support JDK 8 or change the default publication baseline.
