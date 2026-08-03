package io.github.easy4j.validation;

import io.github.easy4j.validation.constraints.AllowableValues;
import io.github.easy4j.validation.constraints.StringDateValue;
import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ValueConstraintTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldRejectDateValuesWithTrailingCharacters() {
        assertEquals(0, validator.validate(new DateSample("2026-08-03")).size());
        assertEquals(1, validator.validate(new DateSample("2026-08-03junk")).size());
        assertEquals(1, validator.validate(new DateSample("2026-02-29")).size());
    }

    @Test
    void shouldTrimConfiguredAllowableValuesAndHandleEmptyConfiguration() {
        assertEquals(0, validator.validate(new AllowableValueSample("professional")).size());
        assertEquals(1, validator.validate(new AllowableValueSample("unknown")).size());
        assertEquals(1, validator.validate(new EmptyAllowableValueSample("value")).size());
    }

    private static final class DateSample {
        @StringDateValue(message = "invalid date")
        private final String value;

        private DateSample(String value) {
            this.value = value;
        }
    }

    private static final class AllowableValueSample {
        @AllowableValues(allows = " basic, professional , enterprise ")
        private final String value;

        private AllowableValueSample(String value) {
            this.value = value;
        }
    }

    private static final class EmptyAllowableValueSample {
        @AllowableValues
        private final String value;

        private EmptyAllowableValueSample(String value) {
            this.value = value;
        }
    }
}
