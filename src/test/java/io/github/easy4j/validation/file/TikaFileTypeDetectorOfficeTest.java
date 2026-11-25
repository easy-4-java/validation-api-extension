package io.github.easy4j.validation.file;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Tika 2 需要容器流才能从内容识别 OLE 内的 Office 类型。 */
class TikaFileTypeDetectorOfficeTest {
    @Test
    void detectsOleExcelFromContentWithoutTrustingFilename() throws Exception {
        byte[] content;
        try (HSSFWorkbook workbook = new HSSFWorkbook();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            workbook.createSheet("data").createRow(0).createCell(0).setCellValue("content");
            workbook.write(output);
            content = output.toByteArray();
        }
        DefaultUploadFile file = new DefaultUploadFile("file", "untrusted.bin", "application/octet-stream",
                content.length, () -> new ByteArrayInputStream(content));
        DetectedFileType detected = new TikaFileTypeDetector().detect(file).orElseThrow(AssertionError::new);
        assertEquals("xls", detected.getExtension());
        assertEquals("application/vnd.ms-excel", detected.getMimeType());
    }
}
