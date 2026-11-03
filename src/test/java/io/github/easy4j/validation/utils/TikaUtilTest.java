package io.github.easy4j.validation.utils;

import io.github.easy4j.validation.file.DefaultUploadFile;
import io.github.easy4j.validation.file.UploadFile;
import org.apache.tika.mime.MimeType;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

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
        byte[] pdfContent = "%PDF-1.7\n%%EOF".getBytes(StandardCharsets.US_ASCII);
        try (InputStream is = new ByteArrayInputStream(pdfContent)) {
            MimeType mimeType = TikaUtil.detectMimeType(is);
            assertEquals("application/pdf", mimeType.getName());
        }
    }

    @Test
    void shouldPreserveMarkableCallerStreamPositionAndOwnership() throws IOException {
        byte[] pdfContent = "%PDF-1.7\n%%EOF".getBytes(StandardCharsets.US_ASCII);
        AtomicBoolean closed = new AtomicBoolean();
        InputStream stream = new ByteArrayInputStream(pdfContent) {
            @Override
            public void close() throws IOException {
                closed.set(true);
                super.close();
            }
        };

        assertEquals("application/pdf", TikaUtil.detectMimeType(stream).getName());
        assertFalse(closed.get(), "the caller owns the stream");
        assertArrayEquals(pdfContent, stream.readAllBytes(), "detection must restore the stream position");
    }

    @Test
    void shouldRestoreBufferedStreamWithoutReadingUnboundedContent() throws IOException {
        byte[] content = new byte[128 * 1024];
        byte[] header = "%PDF-1.7\n%%EOF".getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(header, 0, content, 0, header.length);
        try (InputStream stream = new BufferedInputStream(new ByteArrayInputStream(content))) {
            assertEquals("application/pdf", TikaUtil.detectMimeType(stream).getName());
            assertArrayEquals(header, stream.readNBytes(header.length));
        }
    }

    @Test
    void shouldNotCloseNonMarkableCallerStream() throws IOException {
        byte[] content = "%PDF-1.7\n%%EOF".getBytes(StandardCharsets.US_ASCII);
        ByteArrayInputStream delegate = new ByteArrayInputStream(content);
        AtomicBoolean closed = new AtomicBoolean();
        InputStream stream = new InputStream() {
            @Override
            public int read() {
                return delegate.read();
            }

            @Override
            public int read(byte[] bytes, int offset, int length) {
                return delegate.read(bytes, offset, length);
            }

            @Override
            public void close() {
                closed.set(true);
            }
        };

        assertEquals("application/pdf", TikaUtil.detectMimeType(stream).getName());
        assertFalse(closed.get(), "the caller owns the stream even without mark/reset");
    }

    @Test
    void shouldDetectFileContentDespiteMisleadingExtension() throws IOException {
        Path file = Files.createTempFile("tika-content-", ".txt");
        try {
            Files.write(file, "%PDF-1.7\n%%EOF".getBytes(StandardCharsets.US_ASCII));
            assertEquals("application/pdf", TikaUtil.detectMimeType(file.toFile()).getName());
        } finally {
            Files.deleteIfExists(file);
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
        assertEquals("application/pdf", mimeType.getName());
    }

    @Test
    void shouldIgnoreSpoofedFilenameAndCloseUploadStream() throws IOException {
        byte[] pdfContent = "%PDF-1.7\n%%EOF".getBytes(StandardCharsets.US_ASCII);
        AtomicBoolean closed = new AtomicBoolean();
        UploadFile uploadFile = new DefaultUploadFile("field", "picture.png", "image/png",
                pdfContent.length, () -> new ByteArrayInputStream(pdfContent) {
                    @Override
                    public void close() throws IOException {
                        closed.set(true);
                        super.close();
                    }
                });

        assertEquals("application/pdf", TikaUtil.detectContentMimeType(uploadFile).getName());
        assertTrue(closed.get(), "the upload stream must be closed after detection");
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
