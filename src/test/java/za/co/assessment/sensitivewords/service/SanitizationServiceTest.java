package za.co.assessment.sensitivewords.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.assessment.sensitivewords.config.Constants;
import za.co.assessment.sensitivewords.domain.MatchType;
import za.co.assessment.sensitivewords.domain.SanitizationRequestLog;
import za.co.assessment.sensitivewords.domain.SensitiveWord;
import za.co.assessment.sensitivewords.dto.request.SanitizeTextRequest;
import za.co.assessment.sensitivewords.dto.response.MatchedWordResponse;
import za.co.assessment.sensitivewords.dto.response.SanitizeTextResponse;
import za.co.assessment.sensitivewords.repository.SanitizationRequestLogRepository;
import za.co.assessment.sensitivewords.repository.SensitiveWordRepository;
import za.co.assessment.sensitivewords.service.engine.SanitizationRuleEngine;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SanitizationServiceTest {

    @Mock
    private SensitiveWordRepository sensitiveWordRepository;

    @Mock
    private SanitizationRequestLogRepository requestLogRepository;

    @Mock
    private SanitizationRuleEngine sanitizationRuleEngine;

    @InjectMocks
    private SanitizationService sanitizationService;

    @Test
    void sanitize_shouldReturnOriginalText_whenNoRulesMatch() {
        when(sensitiveWordRepository.findActiveRules(any(LocalDateTime.class))).thenReturn(List.of());
        when(sanitizationRuleEngine.sanitize("Clean message", List.of()))
                .thenReturn(new SanitizationRuleEngine.SanitizationResult("Clean message", List.of()));

        SanitizeTextResponse response = sanitizationService.sanitize(
                new SanitizeTextRequest("Clean message", "unit-test", false)
        );

        assertThat(response.originalText()).isEqualTo("Clean message");
        assertThat(response.sanitizedText()).isEqualTo("Clean message");
        assertThat(response.matchedWordsCount()).isZero();
        assertThat(response.requestId()).isNull();
        verify(requestLogRepository, never()).save(any());
    }

    @Test
    void sanitize_shouldReturnEngineResult() {
        SensitiveWord rule = rule(1L, "testbadword", Constants.DEFAULT_REPLACEMENT, MatchType.CONTAINS, false);
        when(sensitiveWordRepository.findActiveRules(any(LocalDateTime.class))).thenReturn(List.of(rule));
        when(sanitizationRuleEngine.sanitize("This has TESTBADWORD inside", List.of(rule)))
                .thenReturn(new SanitizationRuleEngine.SanitizationResult(
                        "This has *** inside",
                        List.of(new MatchedWordResponse(1L, "testbadword", MatchType.CONTAINS, 1, 1))
                ));

        SanitizeTextResponse response = sanitizationService.sanitize(
                new SanitizeTextRequest("This has TESTBADWORD inside", "unit-test", false)
        );

        assertThat(response.sanitizedText()).isEqualTo("This has *** inside");
        assertThat(response.matchedWordsCount()).isEqualTo(1);
        assertThat(response.matchedWords()).hasSize(1);
    }

    @Test
    void sanitize_shouldPersistRequestLog_whenRequested() {
        SensitiveWord rule = rule(3L, "scam", "[risk-term]", MatchType.CONTAINS, false);
        when(sensitiveWordRepository.findActiveRules(any(LocalDateTime.class))).thenReturn(List.of(rule));
        when(sanitizationRuleEngine.sanitize("Possible scam message", List.of(rule)))
                .thenReturn(new SanitizationRuleEngine.SanitizationResult(
                        "Possible [risk-term] message",
                        List.of(new MatchedWordResponse(3L, "scam", MatchType.CONTAINS, 1, 1))
                ));
        when(requestLogRepository.save(any(SanitizationRequestLog.class))).thenAnswer(invocation -> {
            SanitizationRequestLog log = invocation.getArgument(0);
            log.setRequestId(UUID.randomUUID());
            return log;
        });

        SanitizeTextResponse response = sanitizationService.sanitize(
                new SanitizeTextRequest("Possible scam message", "unit-test", true)
        );

        assertThat(response.requestId()).isNotNull();
        assertThat(response.sanitizedText()).isEqualTo("Possible [risk-term] message");

        ArgumentCaptor<SanitizationRequestLog> captor = ArgumentCaptor.forClass(SanitizationRequestLog.class);
        verify(requestLogRepository).save(captor.capture());
        assertThat(captor.getValue().getMatchedWordsCount()).isEqualTo(1);
    }

    @Test
    void sanitize_shouldNotPersistRequestLog_whenPersistFlagIsMissing() {
        SensitiveWord rule = rule(4L, "scam", "[risk-term]", MatchType.CONTAINS, false);
        when(sensitiveWordRepository.findActiveRules(any(LocalDateTime.class))).thenReturn(List.of(rule));
        when(sanitizationRuleEngine.sanitize("Possible scam message", List.of(rule)))
                .thenReturn(new SanitizationRuleEngine.SanitizationResult(
                        "Possible [risk-term] message",
                        List.of(new MatchedWordResponse(4L, "scam", MatchType.CONTAINS, 1, 1))
                ));

        SanitizeTextResponse response = sanitizationService.sanitize(
                new SanitizeTextRequest("Possible scam message", "unit-test", null)
        );

        assertThat(response.requestId()).isNull();
        verify(requestLogRepository, never()).save(any());
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
