package io.github.easy4j.validation;

import io.github.easy4j.validation.file.DefaultUploadFile;
import io.github.easy4j.validation.file.UploadFile;
import io.github.easy4j.validation.provider.FileContentCheckProvider;
import io.github.easy4j.validation.provider.FileContentCheckStrategy;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileContentCheckStrategyTest {

    @Test
    void shouldUseConfiguredProviderAndFailWhenNoProviderMatches() {
        FileContentCheckStrategy strategy = new FileContentCheckStrategy(Arrays.asList(provider("pdf", true)));
        UploadFile file = new DefaultUploadFile("file", "report.pdf", "application/pdf", 1,
                () -> new ByteArrayInputStream(new byte[] {1}));

        assertTrue(strategy.hasProvider("pdf"));
        assertTrue(strategy.check("pdf", file));
        assertFalse(strategy.hasProvider("docx"));
        assertFalse(strategy.check("docx", file));
    }

    @Test
    void shouldRejectDuplicateProviders() {
        assertThrows(IllegalArgumentException.class, () -> new FileContentCheckStrategy(Arrays.asList(
                provider("pdf", true), provider("pdf", false))));
    }

    private FileContentCheckProvider provider(String support, boolean result) {
        return new FileContentCheckProvider() {
            @Override
            public Boolean check(UploadFile uploadFile) {
                return result;
            }

            @Override
            public String support() {
                return support;
            }
        };
    }
}
