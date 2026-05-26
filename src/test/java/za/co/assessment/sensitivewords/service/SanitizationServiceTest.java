package za.co.assessment.sensitivewords.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.assessment.sensitivewords.domain.SanitizationRequestLog;
import za.co.assessment.sensitivewords.domain.SensitiveWord;
import za.co.assessment.sensitivewords.dto.request.SanitizeTextRequest;
import za.co.assessment.sensitivewords.dto.response.SanitizeTextResponse;
import za.co.assessment.sensitivewords.repository.SanitizationRequestLogRepository;
import za.co.assessment.sensitivewords.service.Impl.SanitizationServiceImpl;
import za.co.assessment.sensitivewords.service.cache.ActiveSensitiveWord;
import za.co.assessment.sensitivewords.service.cache.ActiveSensitiveWordCache;

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
    private ActiveSensitiveWordCache activeSensitiveWordCache;

    @Mock
    private SanitizationRequestLogRepository requestLogRepository;

    @InjectMocks
    private SanitizationServiceImpl sanitizationService;

    @Test
    void sanitize_shouldReturnOriginalText_whenNoRulesMatch() {
        when(activeSensitiveWordCache.getActiveWords()).thenReturn(List.of());

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
    void sanitize_shouldReplaceDbWordsWithDefaultMask() {
        ActiveSensitiveWord rule = word(1L, "testbadword");
        when(activeSensitiveWordCache.getActiveWords()).thenReturn(List.of(rule));

        SanitizeTextResponse response = sanitizationService.sanitize(
                new SanitizeTextRequest("This has TESTBADWORD inside", "unit-test", false)
        );

        assertThat(response.sanitizedText()).isEqualTo("This has *** inside");
        assertThat(response.matchedWordsCount()).isEqualTo(1);
        assertThat(response.matchedWords()).hasSize(1);
        assertThat(response.matchedWords().get(0).word()).isEqualTo("testbadword");
    }

    @Test
    void sanitize_shouldReplaceOnlyExactWords() {
        when(activeSensitiveWordCache.getActiveWords()).thenReturn(List.of(
                word(1L, "on"),
                word(2L, "select")
        ));

        SanitizeTextResponse response = sanitizationService.sanitize(
                new SanitizeTextRequest("hello On select, hello contains Select", "unit-test", false)
        );

        assertThat(response.sanitizedText()).isEqualTo("hello *** ***, hello contains ***");
        assertThat(response.matchedWordsCount()).isEqualTo(3);
    }

    @Test
    void sanitize_shouldReplaceExactPhrasesWithoutMatchingInsideWords() {
        when(activeSensitiveWordCache.getActiveWords()).thenReturn(List.of(word(3L, "restricted phrase")));

        SanitizeTextResponse response = sanitizationService.sanitize(
                new SanitizeTextRequest("restricted phrase unrestricted phrase", "unit-test", false)
        );

        assertThat(response.sanitizedText()).isEqualTo("*** unrestricted phrase");
        assertThat(response.matchedWordsCount()).isEqualTo(1);
    }

    @Test
    void sanitize_shouldPersistRequestLog_whenRequested() {
        ActiveSensitiveWord rule = word(3L, "scam");
        when(activeSensitiveWordCache.getActiveWords()).thenReturn(List.of(rule));
        when(requestLogRepository.save(any(SanitizationRequestLog.class))).thenAnswer(invocation -> {
            SanitizationRequestLog log = invocation.getArgument(0);
            log.setRequestId(UUID.randomUUID());
            return log;
        });

        SanitizeTextResponse response = sanitizationService.sanitize(
                new SanitizeTextRequest("Possible scam message", "unit-test", true)
        );

        assertThat(response.requestId()).isNotNull();
        assertThat(response.sanitizedText()).isEqualTo("Possible *** message");

        ArgumentCaptor<SanitizationRequestLog> captor = ArgumentCaptor.forClass(SanitizationRequestLog.class);
        verify(requestLogRepository).save(captor.capture());
        assertThat(captor.getValue().getMatchedWordsCount()).isEqualTo(1);
    }

    @Test
    void sanitize_shouldNotPersistRequestLog_whenPersistFlagIsMissing() {
        ActiveSensitiveWord rule = word(4L, "scam");
        when(activeSensitiveWordCache.getActiveWords()).thenReturn(List.of(rule));

        SanitizeTextResponse response = sanitizationService.sanitize(
                new SanitizeTextRequest("Possible scam message", "unit-test", null)
        );

        assertThat(response.requestId()).isNull();
        assertThat(response.sanitizedText()).isEqualTo("Possible *** message");
        verify(requestLogRepository, never()).save(any());
    }

    private ActiveSensitiveWord word(Long id, String value) {
        return new ActiveSensitiveWord(id, value, 1);
    }
}
