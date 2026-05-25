package za.co.assessment.sensitivewords.service.engine;

import za.co.assessment.sensitivewords.domain.SensitiveWord;
import za.co.assessment.sensitivewords.dto.response.MatchedWordResponse;

import java.util.List;

public interface SanitizationRuleEngine {

    SanitizationResult sanitize(String inputText, List<SensitiveWord> rules);

    record SanitizationResult(String sanitizedText, List<MatchedWordResponse> matchedWords) {
    }
}
