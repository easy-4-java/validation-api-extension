package io.github.easy4j.validation.utils;

import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class JakartaRegexpUtilsTest {

    @Test
    void shouldMatchFullString() {
        assertTrue(JakartaRegexpUtils.matches("^\\d+$", "12345"));
        assertFalse(JakartaRegexpUtils.matches("^\\d+$", "123abc"));
    }

    @Test
    void shouldGetPattern() {
        Pattern pattern = JakartaRegexpUtils.getPattern("\\d+");
        assertNotNull(pattern);
    }

    @Test
    void shouldReturnNullForBlankPattern() {
        assertNull(JakartaRegexpUtils.getPattern(""));
        assertNull(JakartaRegexpUtils.getPattern(null));
    }

    @Test
    void shouldCachePattern() {
        JakartaRegexpUtils.COMPLIED_PATTERN.clear();
        Pattern p1 = JakartaRegexpUtils.getPattern("\\d+");
        Pattern p2 = JakartaRegexpUtils.getPattern("\\d+");
        assertSame(p1, p2);
    }
}
