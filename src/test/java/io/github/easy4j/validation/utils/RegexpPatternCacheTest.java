package io.github.easy4j.validation.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RegexpPatternCacheTest {

    @Test
    void shouldBoundHistoricalRegularExpressionCaches() {
        JakartaRegexpUtils.COMPLIED_PATTERN.clear();
        RegexpPatternUtils.COMPLIED_PATTERN.clear();
        JakartaOROUtils.COMPLIED_PATTERN.clear();

        for (int index = 0; index <= RegexpPatternCache.MAX_ENTRIES; index++) {
            String pattern = "rule-" + index;
            JakartaRegexpUtils.getPattern(pattern);
            RegexpPatternUtils.getPattern(pattern);
            try {
                JakartaOROUtils.getPattern(pattern, 0);
            } catch (Exception exception) {
                throw new AssertionError(exception);
            }
        }

        assertTrue(JakartaRegexpUtils.COMPLIED_PATTERN.size() <= RegexpPatternCache.MAX_ENTRIES);
        assertTrue(RegexpPatternUtils.COMPLIED_PATTERN.size() <= RegexpPatternCache.MAX_ENTRIES);
        assertTrue(JakartaOROUtils.COMPLIED_PATTERN.size() <= RegexpPatternCache.MAX_ENTRIES);
    }
}
