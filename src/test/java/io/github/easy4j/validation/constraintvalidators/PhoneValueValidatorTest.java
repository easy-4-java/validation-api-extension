package io.github.easy4j.validation.constraintvalidators;

import io.github.easy4j.validation.constraints.PhoneNumber;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PhoneValueValidatorTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldAcceptValidChinesePhoneNumber() {
        assertEquals(0, validator.validate(new Sample("13800138000")).size());
        assertEquals(0, validator.validate(new Sample("15912345678")).size());
    }

    @Test
    void shouldRejectInvalidPhoneNumber() {
        assertEquals(1, validator.validate(new Sample("12345")).size());
        assertEquals(1, validator.validate(new Sample("abcdefghijk")).size());
    }

    @Test
    void shouldAcceptNullValue() {
        assertEquals(0, validator.validate(new Sample(null)).size());
    }

    @Test
    void shouldValidateWithDifferentRegion() {
        assertEquals(0, validator.validate(new USSample("+16502530000")).size());
        assertEquals(1, validator.validate(new USSample("invalid")).size());
    }

    private static class Sample {
        @PhoneNumber(message = "invalid phone")
        private final String value;

        Sample(String value) {
            this.value = value;
        }
    }

    private static class USSample {
        @PhoneNumber(lang = "US", message = "invalid US phone")
        private final String value;

        USSample(String value) {
            this.value = value;
        }
    }
}
