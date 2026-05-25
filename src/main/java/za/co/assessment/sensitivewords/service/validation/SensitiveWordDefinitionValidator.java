package za.co.assessment.sensitivewords.service.validation;

import za.co.assessment.sensitivewords.domain.MatchType;

public interface SensitiveWordDefinitionValidator {

    void validate(String word, MatchType matchType);
}
