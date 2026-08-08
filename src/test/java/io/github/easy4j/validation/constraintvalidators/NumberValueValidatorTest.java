package io.github.easy4j.validation.constraintvalidators;

import io.github.easy4j.validation.constraints.NumberValue;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NumberValueValidatorTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldAcceptValidNumber() {
        assertEquals(0, validator.validate(new Sample("12345")).size());
        assertEquals(0, validator.validate(new Sample("-42")).size());
        assertEquals(0, validator.validate(new Sample("0")).size());
    }

    @Test
    void shouldRejectNonNumericString() {
        assertEquals(1, validator.validate(new Sample("abc")).size());
        assertEquals(1, validator.validate(new Sample("12.34")).size());
        assertEquals(1, validator.validate(new Sample("12a34")).size());
    }

    @Test
    void shouldAcceptNullValue() {
        assertEquals(0, validator.validate(new Sample(null)).size());
    }

    @Test
    void shouldUseCustomRegex() {
        assertEquals(0, validator.validate(new DecimalSample("12.34")).size());
        assertEquals(0, validator.validate(new DecimalSample("0.5")).size());
        assertEquals(1, validator.validate(new DecimalSample("abc")).size());
    }

    private static class Sample {
        @NumberValue(message = "invalid number")
        private final String value;

        Sample(String value) {
            this.value = value;
        }
    }

    private static class DecimalSample {
        @NumberValue(regex = "^[0-9]+(\\.[0-9]+)?$", message = "invalid decimal")
        private final String value;

        DecimalSample(String value) {
            this.value = value;
        }
    }
}
