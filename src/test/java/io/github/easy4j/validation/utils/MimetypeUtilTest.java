package io.github.easy4j.validation.utils;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class MimetypeUtilTest {

    @Test
    void shouldDetectMimeTypeFromFileName() {
        String mimeType = MimetypeUtil.detectMimeType("test.pdf");
        assertNotNull(mimeType);
    }

    @Test
    void shouldReturnNullForNullName() {
        assertNull(MimetypeUtil.detectMimeType((String) null));
    }

    @Test
    void shouldReturnNullForNullFile() {
        assertNull(MimetypeUtil.detectMimeType((File) null));
    }

    @Test
    void shouldReturnNullForNonExistentFile() {
        assertNull(MimetypeUtil.detectMimeType(new File("/nonexistent/file.txt")));
    }
}
