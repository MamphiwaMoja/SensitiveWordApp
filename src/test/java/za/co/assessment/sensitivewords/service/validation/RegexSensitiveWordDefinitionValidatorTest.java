package za.co.assessment.sensitivewords.service.validation;

import org.junit.jupiter.api.Test;
import za.co.assessment.sensitivewords.domain.MatchType;
import za.co.assessment.sensitivewords.web.rest.errors.BadRequestException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegexSensitiveWordDefinitionValidatorTest {

    private final RegexSensitiveWordDefinitionValidator validator = new RegexSensitiveWordDefinitionValidator();

    @Test
    void validate_shouldAcceptNonRegexRules() {
        assertThatCode(() -> validator.validate("[invalid", MatchType.CONTAINS))
                .doesNotThrowAnyException();
    }

    @Test
    void validate_shouldAcceptValidRegexRules() {
        assertThatCode(() -> validator.validate("\\bID\\d{4}\\b", MatchType.REGEX))
                .doesNotThrowAnyException();
    }

    @Test
    void validate_shouldRejectInvalidRegexRules() {
        assertThatThrownBy(() -> validator.validate("[invalid", MatchType.REGEX))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid regex pattern");
    }
}
