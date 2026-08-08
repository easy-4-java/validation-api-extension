package io.github.easy4j.validation.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IDCardUtilsTest {

    @Test
    void shouldValidateCorrect18DigitIdCard() {
        // Valid 18-digit ID card with correct check digit
        assertTrue(IDCardUtils.isIDCard("11010519491231002X"));
    }

    @Test
    void shouldRejectNullIdCard() {
        assertFalse(IDCardUtils.isIDCard(null));
    }

    @Test
    void shouldRejectIdCardWithInvalidLength() {
        assertFalse(IDCardUtils.isIDCard("12345"));
        assertFalse(IDCardUtils.isIDCard("1234567890123456789"));
    }

    @Test
    void shouldVerifyLength() {
        // 15-digit card
        assertTrue(IDCardUtils.verifyLength("110105491231002"));
        // 18-digit card
        assertTrue(IDCardUtils.verifyLength("11010519491231002X"));
        assertFalse(IDCardUtils.verifyLength("12345"));
    }

    @Test
    void shouldVerifyAreaCode() {
        assertTrue(IDCardUtils.verifyAreaCode("11010519491231002X"));
        assertFalse(IDCardUtils.verifyAreaCode("99010519491231002X"));
    }

    @Test
    void shouldVerifyBirthdayCode() {
        assertTrue(IDCardUtils.verifyBirthdayCode("11010519491231002X"));
        assertFalse(IDCardUtils.verifyBirthdayCode("11010519000230002X"));
    }

    @Test
    void shouldVerifyNumber() {
        // 17-digit prefix (first 17 chars of 18-digit card)
        assertTrue(IDCardUtils.verifyNumber("11010519491231002"));
        // 15-digit card (all digits)
        assertTrue(IDCardUtils.verifyNumber("110105491231002"));
        // Letter in 15-digit card
        assertFalse(IDCardUtils.verifyNumber("11010A491231002"));
    }

    @Test
    void shouldVerifyMOD() {
        assertTrue(IDCardUtils.verifyMOD("11010519491231002X"));
        assertFalse(IDCardUtils.verifyMOD("110105194912310020"));
    }

    @Test
    void shouldGetMod() {
        assertEquals("X", IDCardUtils.getMod("11010519491231002"));
        assertEquals("1", IDCardUtils.getMod("1101051949123100"));
    }

    @Test
    void shouldConvert15To18() {
        String result = IDCardUtils.upToEighteen("110105491231002");
        assertNotNull(result);
        assertEquals(18, result.length());
    }

    @Test
    void shouldReturnSameFor18DigitCard() {
        String card = "11010519491231002X";
        assertEquals(card, IDCardUtils.upToEighteen(card));
    }

    @Test
    void shouldGetCodeError() {
        IDCardUtils.isIDCard(null);
        assertNotNull(IDCardUtils.getCodeError());
    }

    @Test
    void shouldRejectIdCardWithLetterInMiddle() {
        assertFalse(IDCardUtils.isIDCard("110105A9491231002X"));
    }
}
