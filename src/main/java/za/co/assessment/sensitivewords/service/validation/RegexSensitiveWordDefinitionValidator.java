package za.co.assessment.sensitivewords.service.validation;

import org.springframework.stereotype.Component;
import za.co.assessment.sensitivewords.domain.MatchType;
import za.co.assessment.sensitivewords.web.rest.errors.BadRequestException;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Component
public class RegexSensitiveWordDefinitionValidator implements SensitiveWordDefinitionValidator {

    @Override
    public void validate(String word, MatchType matchType) {
        if (matchType != MatchType.REGEX) {
            return;
        }

        try {
            Pattern.compile(word);
        } catch (PatternSyntaxException ex) {
            throw new BadRequestException("Invalid regex pattern: " + ex.getDescription());
        }
    }
}
