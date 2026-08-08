package io.github.easy4j.validation.file;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class DefaultUploadFileTest {

    @Test
    void shouldReturnConfiguredProperties() {
        byte[] content = "test content".getBytes();
        DefaultUploadFile file = new DefaultUploadFile("field", "test.txt", "text/plain",
                content.length, () -> new ByteArrayInputStream(content));

        assertEquals("field", file.getName());
        assertEquals("test.txt", file.getOriginalFilename());
        assertEquals("text/plain", file.getContentType());
        assertEquals(content.length, file.getSize());
        assertFalse(file.isEmpty());
    }

    @Test
    void shouldReturnEmptyForZeroSize() {
        DefaultUploadFile file = new DefaultUploadFile("field", "empty.txt", "text/plain",
                0, () -> new ByteArrayInputStream(new byte[0]));
        assertTrue(file.isEmpty());
    }

    @Test
    void shouldReturnEmptyForNegativeSize() {
        DefaultUploadFile file = new DefaultUploadFile("field", "empty.txt", "text/plain",
                -1, () -> new ByteArrayInputStream(new byte[0]));
        assertTrue(file.isEmpty());
    }

    @Test
    void shouldGetBytes() throws IOException {
        byte[] content = "hello world".getBytes();
        DefaultUploadFile file = new DefaultUploadFile("field", "test.txt", "text/plain",
                content.length, () -> new ByteArrayInputStream(content));

        assertArrayEquals(content, file.getBytes());
    }

    @Test
    void shouldGetInputStream() throws IOException {
        byte[] content = "stream test".getBytes();
        DefaultUploadFile file = new DefaultUploadFile("field", "test.txt", "text/plain",
                content.length, () -> new ByteArrayInputStream(content));

        try (InputStream is = file.getInputStream()) {
            assertNotNull(is);
            assertTrue(is.available() > 0);
        }
    }

    @Test
    void shouldThrowOnNullInputStreamSource() {
        assertThrows(NullPointerException.class, () ->
                new DefaultUploadFile("field", "test.txt", "text/plain", 0, null));
    }

    @Test
    void shouldSupportLargeContent() throws IOException {
        byte[] content = new byte[10000];
        for (int i = 0; i < content.length; i++) {
            content[i] = (byte) (i % 256);
        }
        DefaultUploadFile file = new DefaultUploadFile("field", "large.bin",
                "application/octet-stream", content.length,
                () -> new ByteArrayInputStream(content));

        byte[] result = file.getBytes();
        assertEquals(content.length, result.length);
        assertArrayEquals(content, result);
    }
}
