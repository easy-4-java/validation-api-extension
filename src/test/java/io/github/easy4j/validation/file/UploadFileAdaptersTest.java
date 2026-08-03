package io.github.easy4j.validation.file;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertThrows;

class UploadFileAdaptersTest {

    @Test
    void shouldRejectAmbiguousAdaptersAndSupportExplicitReload() {
        UploadFileAdapters.reload();

        assertThrows(IllegalStateException.class, () -> UploadFileAdapters.adapt(new NativeUpload()));
    }

    public static final class NativeUpload {
    }

    public static final class HighPriorityAdapter implements UploadFileAdapter {
        @Override
        public boolean supports(Object value) {
            return value instanceof NativeUpload;
        }

        @Override
        public UploadFile adapt(Object value) {
            return createUploadFile("high");
        }
    }

    public static final class LowPriorityAdapter implements UploadFileAdapter {
        @Override
        public boolean supports(Object value) {
            return value instanceof NativeUpload;
        }

        @Override
        public UploadFile adapt(Object value) {
            return createUploadFile("low");
        }
    }

    private static UploadFile createUploadFile(String name) {
        byte[] bytes = new byte[] {1};
        return new DefaultUploadFile(name, name + ".bin", "application/octet-stream", bytes.length,
                () -> new ByteArrayInputStream(bytes));
    }
}
