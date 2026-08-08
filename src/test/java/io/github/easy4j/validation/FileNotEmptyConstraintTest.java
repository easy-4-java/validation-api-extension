package io.github.easy4j.validation;

import io.github.easy4j.validation.constraints.FileNotEmpty;
import io.github.easy4j.validation.file.DefaultUploadFile;
import io.github.easy4j.validation.file.UploadFile;
import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
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
        UploadFile fiveBytes = file("data.bin", "12345");

        assertEquals(1, validator.validate(new SingleUpload(detectedPdfWithDifferentSuffix)).size());
        assertTrue(validator.validate(new SizeLimitedUpload(fourBytes)).isEmpty());
        assertEquals(1, validator.validate(new SizeLimitedUpload(fiveBytes)).size());
    }

    @Test
    void shouldApplyNonEmptyAndSizeRulesToEveryFileInAnArray() {
        UploadFile empty = file("empty.bin", "");
        UploadFile fourBytes = file("data.bin", "1234");
        UploadFile fiveBytes = file("data.bin", "12345");

        assertEquals(1, validator.validate(new MultipleSizeLimitedUpload(new UploadFile[] {empty})).size());
        assertTrue(validator.validate(new MultipleSizeLimitedUpload(new UploadFile[] {fourBytes})).isEmpty());
        assertEquals(1, validator.validate(new MultipleSizeLimitedUpload(new UploadFile[] {fiveBytes})).size());
    }

    @Test
    void shouldCloseStreamAfterStrictContentDetection() {
        byte[] bytes = "%PDF-1.7\n%%EOF".getBytes(StandardCharsets.US_ASCII);
        TrackingInputStream inputStream = new TrackingInputStream(bytes);
        UploadFile uploadFile = new DefaultUploadFile("file", "report.pdf", "application/pdf", bytes.length,
                () -> inputStream);

        assertTrue(validator.validate(new SingleUpload(uploadFile)).isEmpty());
        assertTrue(inputStream.closed);
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

    private static final class MultipleSizeLimitedUpload {
        @FileNotEmpty(maxSize = "4B")
        private final UploadFile[] files;
        private MultipleSizeLimitedUpload(UploadFile[] files) {
            this.files = files;
        }
    }

    private static final class TrackingInputStream extends ByteArrayInputStream {
        private boolean closed;

        private TrackingInputStream(byte[] bytes) {
            super(bytes);
        }

        @Override
        public void close() throws IOException {
            closed = true;
            super.close();
        }
    }
}
