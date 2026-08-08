package io.github.easy4j.validation.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PatternMatchUtilsTest {

    @Test
    void shouldMatchFullRegex() {
        assertTrue(PatternMatchUtils.matches("^\\d+$", "12345"));
        assertFalse(PatternMatchUtils.matches("^\\d+$", "123abc"));
    }

    @Test
    void shouldSimpleMatchWildcardSuffix() {
        assertTrue(PatternMatchUtils.simpleMatch("test*", "testing"));
        assertTrue(PatternMatchUtils.simpleMatch("test*", "test"));
        assertFalse(PatternMatchUtils.simpleMatch("test*", "other"));
    }

    @Test
    void shouldSimpleMatchWildcardPrefix() {
        assertTrue(PatternMatchUtils.simpleMatch("*test", "mytest"));
        assertFalse(PatternMatchUtils.simpleMatch("*test", "testing"));
    }

    @Test
    void shouldSimpleMatchWildcardBoth() {
        assertTrue(PatternMatchUtils.simpleMatch("*test*", "mytesting"));
        assertFalse(PatternMatchUtils.simpleMatch("*test*", "other"));
    }

    @Test
    void shouldSimpleMatchDirectEquality() {
        assertTrue(PatternMatchUtils.simpleMatch("exact", "exact"));
        assertFalse(PatternMatchUtils.simpleMatch("exact", "other"));
    }

    @Test
    void shouldReturnFalseForNullPattern() {
        assertFalse(PatternMatchUtils.simpleMatch((String) null, "test"));
    }

    @Test
    void shouldReturnFalseForNullString() {
        assertFalse(PatternMatchUtils.simpleMatch("test", null));
    }

    @Test
    void shouldMatchSingleWildcard() {
        assertTrue(PatternMatchUtils.simpleMatch("*", "anything"));
    }

    @Test
    void shouldSimpleMatchWithMultiplePatterns() {
        String[] patterns = {"*.txt", "*.pdf"};
        assertTrue(PatternMatchUtils.simpleMatch(patterns, "test.txt"));
        assertTrue(PatternMatchUtils.simpleMatch(patterns, "doc.pdf"));
        assertFalse(PatternMatchUtils.simpleMatch(patterns, "test.doc"));
    }

    @Test
    void shouldReturnFalseForNullPatterns() {
        assertFalse(PatternMatchUtils.simpleMatch((String[]) null, "test"));
    }

    @Test
    void shouldMatchMiddleWildcard() {
        assertTrue(PatternMatchUtils.simpleMatch("a*b*c", "abc"));
        assertTrue(PatternMatchUtils.simpleMatch("a*b*c", "aXXbYYc"));
        assertFalse(PatternMatchUtils.simpleMatch("a*b*c", "ac"));
    }
}
