package io.github.easy4j.validation;

import io.github.easy4j.validation.constraints.Contains;
import io.github.easy4j.validation.constraints.IdCard;
import io.github.easy4j.validation.constraints.Regexp;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RegexpConstraintsTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @Test
    void validatesRegexpContainsAndIdCard() {
        ValidationSample valid = new ValidationSample("A123", "产康门店", "11010519491231002X");
        assertEquals(0, validator.validate(valid).size());

        ValidationSample invalid = new ValidationSample("B123", "普通门店", "110105194912310021");
        assertEquals(3, validator.validate(invalid).size());
    }

    @Test
    void leavesNullAndBlankHandlingToDedicatedConstraints() {
        ValidationSample empty = new ValidationSample(null, "", null);
        assertEquals(0, validator.validate(empty).size());
    }

    private static class ValidationSample {

        @Regexp(pattern = "^A\\d+$")
        private final String code;

        @Contains(pattern = "产康")
        private final String description;

        @IdCard
        private final String idCard;

        private ValidationSample(String code, String description, String idCard) {
            this.code = code;
            this.description = description;
            this.idCard = idCard;
        }
    }
}
