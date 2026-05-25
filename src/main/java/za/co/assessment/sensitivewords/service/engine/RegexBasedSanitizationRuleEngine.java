package za.co.assessment.sensitivewords.service.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import za.co.assessment.sensitivewords.domain.MatchType;
import za.co.assessment.sensitivewords.domain.SensitiveWord;
import za.co.assessment.sensitivewords.dto.response.MatchedWordResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Component
public class RegexBasedSanitizationRuleEngine implements SanitizationRuleEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegexBasedSanitizationRuleEngine.class);

    @Override
    public SanitizationResult sanitize(String inputText, List<SensitiveWord> rules) {
        String sanitizedText = inputText;
        List<MatchedWordResponse> matchedWords = new ArrayList<>();

        for (SensitiveWord rule : rules) {
            RuleApplicationResult result = applyRule(sanitizedText, rule);
            sanitizedText = result.text();

            if (result.replacementCount() > 0) {
                matchedWords.add(new MatchedWordResponse(
                        rule.getId(),
                        rule.getWord(),
                        rule.getMatchType(),
                        rule.getSeverityLevel(),
                        result.replacementCount()
                ));
            }
        }

        return new SanitizationResult(sanitizedText, matchedWords);
    }

    private RuleApplicationResult applyRule(String input, SensitiveWord rule) {
        Pattern pattern = buildPattern(rule);
        if (pattern == null) {
            return new RuleApplicationResult(input, 0);
        }

        Matcher matcher = pattern.matcher(input);
        StringBuffer buffer = new StringBuffer();
        int count = 0;

        while (matcher.find()) {
            count++;
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(rule.getReplacementValue()));
        }
        matcher.appendTail(buffer);

        return new RuleApplicationResult(buffer.toString(), count);
    }

    private Pattern buildPattern(SensitiveWord rule) {
        int flags = Boolean.TRUE.equals(rule.getCaseSensitive())
                ? 0
                : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;

        try {
            if (rule.getMatchType() == MatchType.REGEX) {
                return Pattern.compile(rule.getWord(), flags);
            }

            String quotedWord = Pattern.quote(rule.getWord());
            if (rule.getMatchType() == MatchType.EXACT) {
                return Pattern.compile("(?<![\\p{L}\\p{N}_])" + quotedWord + "(?![\\p{L}\\p{N}_])", flags);
            }

            return Pattern.compile(quotedWord, flags);
        } catch (PatternSyntaxException ex) {
            LOGGER.warn("Skipping invalid regex rule. sensitiveWordId={}, message={}", rule.getId(), ex.getMessage());
            return null;
        }
    }

    private record RuleApplicationResult(String text, int replacementCount) {
    }
}
