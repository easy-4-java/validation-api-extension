package io.github.easy4j.validation;

import io.github.easy4j.validation.constraints.FileNotEmpty;
import io.github.easy4j.validation.file.DefaultUploadFile;
import io.github.easy4j.validation.file.UploadFile;
import org.junit.jupiter.api.Test;

import javax.validation.Validation;
import javax.validation.Validator;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileNotEmptyConstraintTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldValidateSingleAndMultipleUploadFiles() {
        UploadFile pdf = file("report.pdf", "%PDF-1.7\n%%EOF");
        UploadFile executable = file("report.pdf", "MZ executable");

        assertTrue(validator.validate(new SingleUpload(pdf)).isEmpty());
        assertEquals(1, validator.validate(new SingleUpload(executable)).size());
        assertTrue(validator.validate(new MultipleUpload(new UploadFile[] {pdf, pdf})).isEmpty());
    }

    @Test
    void shouldRejectMissingRequiredFile() {
        assertEquals(1, validator.validate(new SingleUpload(null)).size());
        assertEquals(1, validator.validate(new MultipleUpload(new UploadFile[0])).size());
    }

    @Test
    void shouldPreserveProductionValidationSemantics() {
        UploadFile detectedPdfWithDifferentSuffix = file("report.exe", "%PDF-1.7\n%%EOF");
        UploadFile fourBytes = file("data.bin", "1234");
        UploadFile threeBytes = file("data.bin", "123");

        assertTrue(validator.validate(new SingleUpload(detectedPdfWithDifferentSuffix)).isEmpty());
        assertEquals(1, validator.validate(new SizeLimitedUpload(fourBytes)).size());
        assertTrue(validator.validate(new SizeLimitedUpload(threeBytes)).isEmpty());
    }

    private UploadFile file(String name, String content) {
        byte[] bytes = content.getBytes(StandardCharsets.US_ASCII);
        return new DefaultUploadFile("file", name, "application/pdf", bytes.length,
                () -> new ByteArrayInputStream(bytes));
    }

    private static final class SingleUpload {
        @FileNotEmpty(extensions = "pdf", mimeTypes = "application/pdf", strict = true)
        private final UploadFile file;
        private SingleUpload(UploadFile file) {
            this.file = file;
        }
    }

    private static final class MultipleUpload {
        @FileNotEmpty(extensions = "pdf", mimeTypes = "application/pdf", strict = true)
        private final UploadFile[] files;
        private MultipleUpload(UploadFile[] files) {
            this.files = files;
        }
    }

    private static final class SizeLimitedUpload {
        @FileNotEmpty(maxSize = "4B")
        private final UploadFile file;
        private SizeLimitedUpload(UploadFile file) {
            this.file = file;
        }
    }
}
