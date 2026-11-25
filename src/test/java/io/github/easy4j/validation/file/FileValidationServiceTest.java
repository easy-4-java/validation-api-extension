package io.github.easy4j.validation.file;

import io.github.easy4j.validation.provider.FileContentCheckProvider;
import io.github.easy4j.validation.provider.FileContentCheckStrategy;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 以外部可观察结果约束公共引擎和两个既有入口的语义。 */
class FileValidationServiceTest {
    private final FileValidationService service = new FileValidationService();

    @Test
    void reportsEmptyAndSizeFailuresBeforeOpeningContent() {
        FileValidationPolicy required = FileValidationPolicy.builder().maxSizeBytes(4).build();
        UploadFile exact = file("file.pdf", "application/pdf", "1234");
        assertEquals(FileValidationFailure.EMPTY, service.validate(null, required).getFailure());
        assertTrue(service.validate(null, FileValidationPolicy.builder().required(false).build()).isValid());
        assertTrue(service.validate(exact, FileValidationPolicy.builder().strict(false).maxSizeBytes(4).build()).isValid());
        assertEquals(FileValidationFailure.SIZE_EXCEEDED,
                service.validate(file("file.pdf", "application/pdf", "12345"), required).getFailure());
    }

    @Test
    void preservesUnboundedProgrammaticAndZeroByteConstraintLimits() {
        UploadFile file = file("data.bin", "application/octet-stream", "data");
        assertTrue(service.validate(file, FileValidationPolicy.builder().strict(false).maxSizeBytes(0).build()).isValid());
        assertEquals(FileValidationFailure.SIZE_EXCEEDED, service.validate(file,
                FileValidationPolicy.builder().constraintCompatibility(true).maxSizeBytes(0).build()).getFailure());
    }

    @Test
    void normalizedEmptyConstraintAllowlistStillMeansUnrestricted() {
        assertTrue(service.validate(file("data.bin", "application/octet-stream", "data"),
                FileValidationPolicy.builder().constraintCompatibility(true)
                        .allowedExtensions(".", " ").build()).isValid());
    }

    @Test
    void preservesExtensionSignatureAndMimeFailureReasons() {
        FileValidationPolicy pdf = FileValidationPolicy.builder().allowedExtensions("pdf")
                .allowedMimeTypes("application/pdf").build();
        assertEquals(FileValidationFailure.EXTENSION_NOT_ALLOWED,
                service.validate(file("file.exe", "application/pdf", "%PDF-1.7"), pdf).getFailure());
        FileValidationResult wrong = service.validate(file("file.pdf", "application/pdf", "plain text"), pdf);
        assertEquals(FileValidationFailure.SIGNATURE_MISMATCH, wrong.getFailure());
        assertEquals("text/plain", wrong.getDetectedType().getMimeType());
        assertEquals(FileValidationFailure.MIME_TYPE_NOT_ALLOWED,
                service.validate(file("file.pdf", "image/png", "%PDF-1.7"),
                        FileValidationPolicy.builder().strict(false).allowedMimeTypes("application/pdf").build()).getFailure());
    }

    @Test
    void onlyConstraintModeUsesFilenameMimeAndSkipsUnrestrictedDetection() {
        UploadFile disguised = file("file.txt", "image/png", "plain text");
        FileValidationPolicy compatibility = FileValidationPolicy.builder().constraintCompatibility(true)
                .strict(false).allowedMimeTypes("text/plain").build();
        assertTrue(service.validate(disguised, compatibility).isValid());
        AtomicInteger detections = new AtomicInteger();
        FileValidationService counting = new FileValidationService(file -> {
            detections.incrementAndGet();
            return Optional.empty();
        }, new FileContentCheckStrategy(List.of()), (file, type) -> true);
        assertTrue(counting.validate(disguised,
                FileValidationPolicy.builder().constraintCompatibility(true).build()).isValid());
        assertEquals(0, detections.get());
        assertEquals(FileValidationFailure.TYPE_UNDETECTABLE,
                counting.validate(disguised, FileValidationPolicy.builder().build()).getFailure());
        assertEquals(1, detections.get());
    }

    @Test
    void mimeOnlyConstraintAllowsDifferentSuffixWhileDocumentPolicyRejectsIt() {
        UploadFile pdf = file("file.bin", "application/octet-stream", "%PDF-1.7");
        assertTrue(service.validate(pdf, FileValidationPolicy.builder().constraintCompatibility(true)
                .allowedMimeTypes("application/pdf").build()).isValid());
        assertEquals(FileValidationFailure.SIGNATURE_MISMATCH, service.validate(pdf,
                FileValidationPolicy.builder().allowedMimeTypes("application/pdf").build()).getFailure());
    }

    @Test
    void legacyGenericWordContainerIsConfinedToConstraintCompatibility() {
        FileValidationService generic = new FileValidationService(file -> Optional.of(
                new DetectedFileType("", "application/x-tika-msoffice")),
                new FileContentCheckStrategy(List.of()), (file, type) -> true);
        UploadFile doc = file("file.doc", "application/msword", "data");
        assertTrue(generic.validate(doc, FileValidationPolicy.builder().constraintCompatibility(true)
                .allowedExtensions(".DOC").allowedMimeTypes("application/msword").build()).isValid());
        assertFalse(generic.validate(doc, FileValidationPolicy.builder()
                .allowedExtensions("doc").allowedMimeTypes("application/msword").build()).isValid());
    }

    @Test
    void contentRejectionKeepsDetectedTypeAndReadErrorFailsClosed() {
        FileContentCheckProvider deny = new FileContentCheckProvider() {
            @Override
            public Boolean check(UploadFile file) {
                return false;
            }
            @Override
            public String support() {
                return "pdf";
            }
        };
        FileValidationService rejecting = new FileValidationService(new TikaFileTypeDetector(),
                new FileContentCheckStrategy(List.of(deny)), (file, type) -> true);
        FileValidationResult result = rejecting.validate(file("file.pdf", "application/pdf", "%PDF-1.7"),
                FileValidationPolicy.builder().build());
        assertEquals(FileValidationFailure.CONTENT_REJECTED, result.getFailure());
        assertEquals("application/pdf", result.getDetectedType().getMimeType());
        UploadFile unreadable = new DefaultUploadFile("file", "file.pdf", "application/pdf", 10,
                () -> { throw new IOException("unreadable"); });
        assertEquals(FileValidationFailure.READ_ERROR,
                service.validate(unreadable, FileValidationPolicy.builder().build()).getFailure());
    }

    @Test
    void additionalBusinessRulesRunOnlyAfterBasicRulesAndSpiPass() {
        AtomicInteger calls = new AtomicInteger();
        FileValidationService rejecting = new FileValidationService(new TikaFileTypeDetector(),
                new FileContentCheckStrategy(List.of()), (file, type) -> {
                    calls.incrementAndGet();
                    return false;
                });
        FileValidationPolicy policy = FileValidationPolicy.builder().allowedExtensions("pdf").build();
        assertEquals(FileValidationFailure.EXTENSION_NOT_ALLOWED,
                rejecting.validate(file("file.exe", "application/pdf", "%PDF-1.7"), policy).getFailure());
        assertEquals(0, calls.get());
        assertEquals(FileValidationFailure.CONTENT_REJECTED,
                rejecting.validate(file("file.pdf", "application/pdf", "%PDF-1.7"), policy).getFailure());
        assertEquals(1, calls.get());
    }

    private static UploadFile file(String name, String mime, String content) {
        byte[] bytes = content.getBytes(StandardCharsets.US_ASCII);
        return new DefaultUploadFile("file", name, mime, bytes.length, () -> new ByteArrayInputStream(bytes));
    }
}
