package io.github.easy4j.validation.constraintvalidators;

import io.github.easy4j.validation.constraints.AllowableValues;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AllowedValuesValidatorTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldAcceptValueInAllowedSet() {
        assertEquals(0, validator.validate(new Sample("basic")).size());
        assertEquals(0, validator.validate(new Sample("professional")).size());
        assertEquals(0, validator.validate(new Sample("enterprise")).size());
    }

    @Test
    void shouldRejectValueNotInAllowedSet() {
        assertEquals(1, validator.validate(new Sample("unknown")).size());
        assertEquals(1, validator.validate(new Sample("")).size());
    }

    @Test
    void shouldAcceptBlankValueWhenNullable() {
        assertEquals(0, validator.validate(new NullableSample(null)).size());
        assertEquals(0, validator.validate(new NullableSample("")).size());
        assertEquals(0, validator.validate(new NullableSample("  ")).size());
    }

    @Test
    void shouldRejectBlankValueWhenNotNullable() {
        assertEquals(1, validator.validate(new Sample(null)).size());
        assertEquals(1, validator.validate(new Sample("  ")).size());
    }

    @Test
    void shouldHandleEmptyAllowsString() {
        assertEquals(1, validator.validate(new EmptySample("anything")).size());
    }

    private static class Sample {
        @AllowableValues(allows = "basic, professional, enterprise")
        private final String value;

        Sample(String value) {
            this.value = value;
        }
    }

    private static class NullableSample {
        @AllowableValues(allows = "basic, professional", nullable = true)
        private final String value;

        NullableSample(String value) {
            this.value = value;
        }
    }

    private static class EmptySample {
        @AllowableValues
        private final String value;

        EmptySample(String value) {
            this.value = value;
        }
    }
}
