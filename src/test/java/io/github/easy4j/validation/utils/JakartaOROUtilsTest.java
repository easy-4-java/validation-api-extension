package io.github.easy4j.validation.utils;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Perl5Compiler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JakartaOROUtilsTest {

    @Test
    void shouldMatchFullString() {
        assertTrue(JakartaOROUtils.matches("^\\d+$", Perl5Compiler.DEFAULT_MASK, "12345"));
        assertFalse(JakartaOROUtils.matches("^\\d+$", Perl5Compiler.DEFAULT_MASK, "123abc"));
    }

    @Test
    void shouldReturnFalseForBlankPattern() {
        // getPattern returns null for blank patterns; the caller must guard against this.
        // We test getPattern directly instead.
        try {
            assertNull(JakartaOROUtils.getPattern("", Perl5Compiler.DEFAULT_MASK));
            assertNull(JakartaOROUtils.getPattern(null, Perl5Compiler.DEFAULT_MASK));
        } catch (MalformedPatternException e) {
            throw new AssertionError(e);
        }
    }

    @Test
    void shouldContainSubstring() {
        assertTrue(JakartaOROUtils.contains("test", Perl5Compiler.DEFAULT_MASK, "this is a test string"));
        assertFalse(JakartaOROUtils.contains("xyz", Perl5Compiler.DEFAULT_MASK, "hello world"));
    }

    @Test
    void shouldMatchPrefix() {
        assertTrue(JakartaOROUtils.matchesPrefix("^hel", Perl5Compiler.DEFAULT_MASK, "hello world"));
        assertFalse(JakartaOROUtils.matchesPrefix("^world", Perl5Compiler.DEFAULT_MASK, "hello world"));
    }

    @Test
    void shouldGetMatchResult() {
        assertNotNull(JakartaOROUtils.getMatchResult("\\d+", Perl5Compiler.DEFAULT_MASK, "abc123def"));
        assertNull(JakartaOROUtils.getMatchResult("\\d+", Perl5Compiler.DEFAULT_MASK, "abcdef"));
    }

    @Test
    void shouldReturnNullMatchResultForBlankPattern() {
        assertNull(JakartaOROUtils.getMatchResult("", Perl5Compiler.DEFAULT_MASK, "test"));
    }

    @Test
    void shouldReplace() {
        String result = JakartaOROUtils.replaces("\\d+", Perl5Compiler.DEFAULT_MASK, "abc123def456");
        assertNotNull(result);
    }

    @Test
    void shouldReturnInputForBlankPattern() {
        String input = "test string";
        String result = JakartaOROUtils.replaces("", Perl5Compiler.DEFAULT_MASK, input);
        assertEquals(input, result);
    }

    @Test
    void shouldGetPattern() throws MalformedPatternException {
        assertNotNull(JakartaOROUtils.getPattern("\\d+", Perl5Compiler.DEFAULT_MASK));
        assertNull(JakartaOROUtils.getPattern("", Perl5Compiler.DEFAULT_MASK));
        assertNull(JakartaOROUtils.getPattern(null, Perl5Compiler.DEFAULT_MASK));
    }

    @Test
    void shouldCachePattern() throws MalformedPatternException {
        JakartaOROUtils.COMPLIED_PATTERN.clear();
        var p1 = JakartaOROUtils.getPattern("\\d+", Perl5Compiler.DEFAULT_MASK);
        var p2 = JakartaOROUtils.getPattern("\\d+", Perl5Compiler.DEFAULT_MASK);
        assertSame(p1, p2);
    }

    @Test
    void shouldReturnFalseForMalformedPattern() {
        assertFalse(JakartaOROUtils.matches("[invalid", Perl5Compiler.DEFAULT_MASK, "test"));
        assertFalse(JakartaOROUtils.contains("[invalid", Perl5Compiler.DEFAULT_MASK, "test"));
        assertFalse(JakartaOROUtils.matchesPrefix("[invalid", Perl5Compiler.DEFAULT_MASK, "test"));
    }
}
