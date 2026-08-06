# validation-api-extension

[English](./README.md) | [简体中文](./README.zh-CN.md)

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
- Not a Jakarta Validation (`jakarta.validation`) build on the 1.0.x line — the 3.0.x line switches to Jakarta namespaces (see Section 10).
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
| API compatibility gate | Stable | Clirr `api-compatibility` profile + CI job guard the public API surface |

## 3. Requirements & Compatibility

| Requirement | Version |
|:---|:---|
| JDK | 21+ (baseline of the `feature/3.0.x` branch, `maven.compiler.release=8`) |
| Maven | 3.0+ |
| Bean Validation API | `javax.validation:validation-api` 2.0.1.Final |
| Runtime provider | Any JSR-380 provider (tests use Hibernate Validator 6.2.4.Final) |

**Version line matrix**

| Branch | JDK | Bean Validation API | Version pattern |
|:---|:---|:---|:---|
| `feature/1.0.x` | 8 | Javax Validation 2.0.1 | `1.0.x.*` |
| `feature/2.0.x` | 17 | Javax Validation 2.0.1 | `2.0.x.*` |
| `feature/3.0.x` | 21 | Jakarta Validation 3.1.1 | `3.0.x.*` |

Besides the `javax` / `jakarta` namespace adaptation, the three lines keep the same public API, implementation and documentation. Dependency baselines evolve per line (verified from the branch POMs):

| Key dependency | `1.0.x` | `2.0.x` | `3.0.x` |
|:---|:---|:---|:---|
| Apache Tika | 2.9.4 | 3.3.1 | 3.3.2 |
| libphonenumber | 9.0.34 | 9.0.35 | 9.0.36 |
| Apache Commons IO | 2.22.0 | 2.22.0 | 2.22.0 |
| Hutool Core | 5.8.45 | 5.8.46 | 5.8.47 |
| Hibernate Validator (test) | 6.2.4.Final | 6.2.5.Final | 9.1.3.Final |

The `1.0.x` line pins Apache Tika at **2.9.4**, the newest Tika version loadable on JDK 8 (class major version 52). Tika 3.2.2+ fixes known XXE issues but requires JDK 11, so it cannot enter the JDK 8 line.

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

> **Assumption**: artifacts are currently distributed through the project's private Maven repository (Aliyun) and GitHub Releases; the library is **not yet published to Maven Central**. If the coordinates below cannot be resolved, either add the private repository to your build or install locally with `./mvnw install`.

**Maven**

```xml
<dependency>
    <groupId>io.github.easy4j</groupId>
    <artifactId>validation-api-extension</artifactId>
    <version>3.0.x.x.20260630-SNAPSHOT</version>
</dependency>
```

**Gradle**

```gradle
implementation 'io.github.easy4j:validation-api-extension:3.0.x.x.20260630-SNAPSHOT'
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
MimeType mime = TikaUtil.detectMimeType(uploadFile);   // UploadFile, File or InputStream
String mimeType = MimetypeUtil.detectMimeType(file);   // string variant by file or name

// Run registered SPI content providers for a given extension
FileContentCheckStrategy strategy = FileContentCheckStrategy.load();
if (strategy.hasProvider("pdf")) {
    boolean ok = strategy.check("pdf", uploadFile);
}
```

## 9. Testing & Build

```bash
./mvnw clean verify     # unit tests + JaCoCo coverage report
./mvnw -Papi-compatibility -Dapi.compatibility.version=<baseline> verify   # Clirr public-API check
```

- **Tests**: 6 test classes / 13 `@Test` methods covering constraints, the file validation engine and the SPI adapter discovery.
- **Coverage gate**: JaCoCo checks a 90% line-coverage minimum at the `verify` phase (`haltOnFailure=false`).
- **CI** (`.github/workflows/build.yml`): JDK 8 matrix running `mvn -B -ntp clean verify`, dependency review on PRs, and an API-compatibility job that diffs the current public API against the target branch with Clirr.

## 10. Versioning & Branches

| Branch | JDK | Validation API | Version pattern |
|:---|:---|:---|:---|
| `feature/1.0.x` | 8 | Javax Validation 2.0.1 | `1.0.x.*` |
| `feature/2.0.x` | 17 | Javax Validation 2.0.1 | `2.0.x.*` |
| `feature/3.0.x` | 21 | Jakarta Validation 3.1.1 | `3.0.x.*` |

- Snapshot versions follow the `1.0.x.yyyyMMdd-SNAPSHOT` scheme; releases are tagged `v{version}` and published through the project's private repository and GitHub Releases.
- The `1.0.x` line is the actively maintained JDK 8 / Javax line. The 2.0.x line upgrades the JDK baseline to 17 while keeping the Javax namespace; the 3.0.x line moves to Jakarta Validation 3.x on JDK 21.

## 11. Contributing & License

Contributions are welcome — please open an issue or a pull request on GitHub.

This project is licensed under the **Apache License, Version 2.0**. See the [LICENSE](./LICENSE) file for details.
