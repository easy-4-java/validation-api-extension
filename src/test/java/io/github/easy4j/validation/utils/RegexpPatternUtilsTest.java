package io.github.easy4j.validation.utils;

import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class RegexpPatternUtilsTest {

    @Test
    void shouldGetPattern() {
        Pattern pattern = RegexpPatternUtils.getPattern("\\d+");
        assertNotNull(pattern);
        assertTrue(pattern.matcher("123").matches());
    }

    @Test
    void shouldReturnNullForBlankPattern() {
        assertNull(RegexpPatternUtils.getPattern(""));
        assertNull(RegexpPatternUtils.getPattern(null));
    }

    @Test
    void shouldCachePattern() {
        RegexpPatternUtils.COMPLIED_PATTERN.clear();
        Pattern p1 = RegexpPatternUtils.getPattern("\\d+");
        Pattern p2 = RegexpPatternUtils.getPattern("\\d+");
        assertSame(p1, p2);
    }
}
