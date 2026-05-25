package za.co.assessment.sensitivewords.service.engine;

import org.junit.jupiter.api.Test;
import za.co.assessment.sensitivewords.config.Constants;
import za.co.assessment.sensitivewords.domain.MatchType;
import za.co.assessment.sensitivewords.domain.SensitiveWord;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RegexBasedSanitizationRuleEngineTest {

    private final RegexBasedSanitizationRuleEngine engine = new RegexBasedSanitizationRuleEngine();

    @Test
    void sanitize_shouldReplaceContainsRule_caseInsensitive() {
        SensitiveWord rule = rule(1L, "testbadword", Constants.DEFAULT_REPLACEMENT, MatchType.CONTAINS, false);

        SanitizationRuleEngine.SanitizationResult result = engine.sanitize(
                "This has TESTBADWORD inside",
                List.of(rule)
        );

        assertThat(result.sanitizedText()).isEqualTo("This has *** inside");
        assertThat(result.matchedWords()).hasSize(1);
        assertThat(result.matchedWords().get(0).replacementCount()).isEqualTo(1);
    }

    @Test
    void sanitize_shouldReplaceExactRuleOnlyWhenTokenMatches() {
        SensitiveWord rule = rule(2L, "cat", "[animal]", MatchType.EXACT, false);

        SanitizationRuleEngine.SanitizationResult result = engine.sanitize(
                "cat concatenate cat",
                List.of(rule)
        );

        assertThat(result.sanitizedText()).isEqualTo("[animal] concatenate [animal]");
        assertThat(result.matchedWords()).hasSize(1);
        assertThat(result.matchedWords().get(0).replacementCount()).isEqualTo(2);
    }

    @Test
    void sanitize_shouldRespectCaseSensitiveRules() {
        SensitiveWord rule = rule(5L, "Secret", "[masked]", MatchType.CONTAINS, true);

        SanitizationRuleEngine.SanitizationResult result = engine.sanitize(
                "This contains secret but not the exact case",
                List.of(rule)
        );

        assertThat(result.sanitizedText()).isEqualTo("This contains secret but not the exact case");
        assertThat(result.matchedWords()).isEmpty();
    }

    @Test
    void sanitize_shouldApplyMultipleRules() {
        SensitiveWord containsRule = rule(6L, "scam", "[risk-term]", MatchType.CONTAINS, false);
        SensitiveWord exactRule = rule(7L, "restricted phrase", "[restricted]", MatchType.EXACT, false);

        SanitizationRuleEngine.SanitizationResult result = engine.sanitize(
                "Possible scam and restricted phrase",
                List.of(containsRule, exactRule)
        );

        assertThat(result.sanitizedText()).isEqualTo("Possible [risk-term] and [restricted]");
        assertThat(result.matchedWords()).hasSize(2);
    }

    @Test
    void sanitize_shouldSkipInvalidRegexRuleWithoutFailingRequest() {
        SensitiveWord invalidRegexRule = rule(8L, "[unclosed", "[masked]", MatchType.REGEX, false);

        SanitizationRuleEngine.SanitizationResult result = engine.sanitize(
                "Original text",
                List.of(invalidRegexRule)
        );

        assertThat(result.sanitizedText()).isEqualTo("Original text");
        assertThat(result.matchedWords()).isEmpty();
    }

    private SensitiveWord rule(Long id, String word, String replacement, MatchType matchType, boolean caseSensitive) {
        SensitiveWord rule = new SensitiveWord();
        rule.setId(id);
        rule.setWord(word);
        rule.setReplacementValue(replacement);
        rule.setMatchType(matchType);
        rule.setSeverityLevel(1);
        rule.setCaseSensitive(caseSensitive);
        rule.setActive(true);
        return rule;
    }
}
