package io.github.easy4j.validation;

import io.github.easy4j.validation.file.DefaultValidatableFile;
import io.github.easy4j.validation.file.FileValidationFailure;
import io.github.easy4j.validation.file.FileValidationPolicy;
import io.github.easy4j.validation.file.FileValidationResult;
import io.github.easy4j.validation.file.FileValidationService;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileValidationServiceTest {

    private final FileValidationService validationService = new FileValidationService();

    @Test
    void shouldDetectPdfFromContentHeader() {
        byte[] content = "%PDF-1.7\n1 0 obj\n<<>>\nendobj\n%%EOF"
                .getBytes(StandardCharsets.US_ASCII);

        FileValidationResult result = validationService.validate(
                file("report.pdf", "application/octet-stream", content), policy());

        assertTrue(result.isValid());
        assertEquals("application/pdf", result.getDetectedFileType().getMimeType());
    }

    @Test
    void shouldRejectExecutableRenamedAsPdf() {
        byte[] content = new byte[] {0x4D, 0x5A, (byte) 0x90, 0x00, 0x03, 0x00};

        FileValidationResult result = validationService.validate(
                file("report.pdf", "application/pdf", content), policy());

        assertEquals(FileValidationFailure.SIGNATURE_MISMATCH, result.getFailure());
    }

    @Test
    void shouldDetectSupportedOfficeContainers() throws IOException {
        assertDetected("report.doc", createDoc(), "application/msword");
        assertDetected("report.docx", createDocx(),
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        assertDetected("report.xls", createXls(), "application/vnd.ms-excel");
        assertDetected("report.xlsx", createXlsx(),
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @Test
    void shouldApplyRequiredAndMaximumSizeRules() {
        FileValidationPolicy optional = FileValidationPolicy.builder().required(false).build();
        FileValidationPolicy oneByte = FileValidationPolicy.builder().maxSizeBytes(1).build();

        assertTrue(validationService.validate(null, optional).isValid());
        assertEquals(FileValidationFailure.SIZE_EXCEEDED,
                validationService.validate(file("report.pdf", "application/pdf", new byte[] {1, 2}),
                        oneByte).getFailure());
    }

    private FileValidationPolicy policy() {
        return FileValidationPolicy.builder()
                .allowedExtensions("doc", "docx", "xls", "xlsx", "pdf")
                .allowedMimeTypes(
                        "application/msword",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                        "application/vnd.ms-excel",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        "application/pdf")
                .build();
    }

    private void assertDetected(String fileName, byte[] content, String expectedMimeType) {
        FileValidationResult result = validationService.validate(
                file(fileName, "application/octet-stream", content), policy());

        assertTrue(result.isValid(), () -> fileName + " detection failed: " + result.getFailure()
                + ", detected=" + (result.getDetectedFileType() == null ? "null"
                : result.getDetectedFileType().getMimeType() + result.getDetectedFileType().getExtensions()));
        assertEquals(expectedMimeType, result.getDetectedFileType().getMimeType());
    }

    private byte[] createDocx() throws IOException {
        try (XWPFDocument document = new XWPFDocument();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            document.createParagraph().createRun().setText("validation-api-extension");
            document.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private byte[] createDoc() throws IOException {
        try (POIFSFileSystem fileSystem = new POIFSFileSystem();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            fileSystem.getRoot().createDocument("WordDocument",
                    new ByteArrayInputStream(new byte[4096]));
            fileSystem.writeFilesystem(outputStream);
            return outputStream.toByteArray();
        }
    }

    private byte[] createXls() throws IOException {
        try (HSSFWorkbook workbook = new HSSFWorkbook();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            workbook.createSheet("validation").createRow(0).createCell(0)
                    .setCellValue("validation-api-extension");
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private byte[] createXlsx() throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            workbook.createSheet("validation").createRow(0).createCell(0)
                    .setCellValue("validation-api-extension");
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private DefaultValidatableFile file(String fileName, String contentType, byte[] content) {
        return new DefaultValidatableFile(fileName, contentType, content.length,
                () -> new ByteArrayInputStream(content));
    }
}
