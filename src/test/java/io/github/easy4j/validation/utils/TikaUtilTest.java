package io.github.easy4j.validation.utils;

import io.github.easy4j.validation.file.DefaultUploadFile;
import io.github.easy4j.validation.file.UploadFile;
import org.apache.tika.mime.MimeType;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class TikaUtilTest {

    @Test
    void shouldReturnNullForNullFile() throws IOException {
        assertNull(TikaUtil.detectMimeType((File) null));
    }

    @Test
    void shouldReturnNullForNonExistentFile() throws IOException {
        assertNull(TikaUtil.detectMimeType(new File("/nonexistent/file.txt")));
    }

    @Test
    void shouldReturnNullForNullInputStream() throws IOException {
        assertNull(TikaUtil.detectMimeType((InputStream) null));
    }

    @Test
    void shouldDetectMimeTypeFromInputStream() throws IOException {
        byte[] pdfContent = "%PDF-1.7\n%%EOF".getBytes();
        try (InputStream is = new ByteArrayInputStream(pdfContent)) {
            MimeType mimeType = TikaUtil.detectMimeType(is);
            assertNotNull(mimeType);
        }
    }

    @Test
    void shouldReturnNullForNullUploadFile() throws IOException {
        assertNull(TikaUtil.detectMimeType((UploadFile) null));
    }

    @Test
    void shouldReturnNullForEmptyUploadFile() throws IOException {
        UploadFile emptyFile = new DefaultUploadFile("field", "empty.txt", "text/plain",
                0, () -> new ByteArrayInputStream(new byte[0]));
        assertNull(TikaUtil.detectMimeType(emptyFile));
    }

    @Test
    void shouldDetectMimeTypeFromUploadFile() throws IOException {
        byte[] pdfContent = "%PDF-1.7\n%%EOF".getBytes();
        UploadFile uploadFile = new DefaultUploadFile("field", "test.pdf", "application/pdf",
                pdfContent.length, () -> new ByteArrayInputStream(pdfContent));
        MimeType mimeType = TikaUtil.detectMimeType(uploadFile);
        assertNotNull(mimeType);
    }

    @Test
    void shouldDetectContentMimeType() throws IOException {
        byte[] pdfContent = "%PDF-1.7\n%%EOF".getBytes();
        UploadFile uploadFile = new DefaultUploadFile("field", "test.pdf", "application/pdf",
                pdfContent.length, () -> new ByteArrayInputStream(pdfContent));
        MimeType mimeType = TikaUtil.detectContentMimeType(uploadFile);
        assertNotNull(mimeType);
    }

    @Test
    void shouldReturnNullContentMimeTypeForNullUploadFile() throws IOException {
        assertNull(TikaUtil.detectContentMimeType(null));
    }

    @Test
    void shouldReturnNullContentMimeTypeForEmptyUploadFile() throws IOException {
        UploadFile emptyFile = new DefaultUploadFile("field", "empty.txt", "text/plain",
                0, () -> new ByteArrayInputStream(new byte[0]));
        assertNull(TikaUtil.detectContentMimeType(emptyFile));
    }
}
