package io.github.easy4j.validation.utils;

import io.github.easy4j.validation.file.DefaultUploadFile;
import io.github.easy4j.validation.file.UploadFile;
import org.apache.tika.mime.MimeType;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
    void shouldRecognizeJarWithManifestBeyondFirst64KiB() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ZipOutputStream archive = new ZipOutputStream(output)) {
            archive.putNextEntry(new ZipEntry("payload.bin"));
            byte[] filler = new byte[70 * 1024];
            new Random(1234).nextBytes(filler);
            archive.write(filler);
            archive.closeEntry();
            archive.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
            archive.write("Manifest-Version: 1.0\r\n\r\n".getBytes(StandardCharsets.US_ASCII));
            archive.closeEntry();
        }
        byte[] jar = output.toByteArray();
        assertTrue(jar.length > 64 * 1024);
        try (InputStream stream = new ByteArrayInputStream(jar)) {
            assertEquals("application/java-archive", TikaUtil.detectMimeType(stream).getName());
            assertArrayEquals(jar, stream.readAllBytes());
        }
    }

    @Test
    void shouldRecognizeOfficeContainersWithMediaBeforeContentTypes() throws IOException {
        byte[] docx = officeArchive("word", "document.xml",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml",
                "<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:body/></w:document>");
        byte[] xlsx = officeArchive("xl", "workbook.xml",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml",
                "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"/>");

        assertOfficeMimeType(docx, "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        assertOfficeMimeType(xlsx, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    private static void assertOfficeMimeType(byte[] content, String expectedType) throws IOException {
        assertTrue(content.length > 64 * 1024);
        try (InputStream stream = new BufferedInputStream(new ByteArrayInputStream(content))) {
            assertEquals(expectedType, TikaUtil.detectMimeType(stream).getName());
            assertArrayEquals(content, stream.readAllBytes());
        }
    }

    private static byte[] officeArchive(String directory, String documentName,
            String contentType, String documentXml) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ZipOutputStream archive = new ZipOutputStream(output)) {
            byte[] media = new byte[70 * 1024];
            new Random(1234).nextBytes(media);
            writeZipEntry(archive, directory + "/media/image1.png", media);
            String documentPath = directory + "/" + documentName;
            String types = "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
                    + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                    + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
                    + "<Override PartName=\"/" + documentPath + "\" ContentType=\"" + contentType + "\"/>"
                    + "</Types>";
            writeZipEntry(archive, "[Content_Types].xml", types.getBytes(StandardCharsets.UTF_8));
            String relationships = "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                    + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\""
                    + " Target=\"" + documentPath + "\"/></Relationships>";
            writeZipEntry(archive, "_rels/.rels", relationships.getBytes(StandardCharsets.UTF_8));
            writeZipEntry(archive, documentPath, documentXml.getBytes(StandardCharsets.UTF_8));
        }
        return output.toByteArray();
    }

    private static void writeZipEntry(ZipOutputStream archive, String path, byte[] content)
            throws IOException {
        archive.putNextEntry(new ZipEntry(path));
        archive.write(content);
        archive.closeEntry();
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
    void shouldRejectOversizedBufferedStreamWithoutLosingItsPosition() throws IOException {
        byte[] content = new byte[1024 * 1024 + 1];
        byte[] header = "%PDF-1.7\n%%EOF".getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(header, 0, content, 0, header.length);
        try (InputStream stream = new BufferedInputStream(new ByteArrayInputStream(content))) {
            IOException error = assertThrows(IOException.class, () -> TikaUtil.detectMimeType(stream));
            assertTrue(error.getMessage().contains("1 MiB"));
            assertArrayEquals(header, stream.readNBytes(header.length));
        }
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
