package za.co.assessment.sensitivewords.service;

import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import za.co.assessment.sensitivewords.domain.SanitizationRequestLog;
import za.co.assessment.sensitivewords.domain.SensitiveWord;
import za.co.assessment.sensitivewords.dto.request.SanitizeTextRequest;
import za.co.assessment.sensitivewords.dto.response.SanitizeTextResponse;
import za.co.assessment.sensitivewords.repository.SanitizationRequestLogRepository;
import za.co.assessment.sensitivewords.repository.SensitiveWordRepository;
import za.co.assessment.sensitivewords.service.engine.SanitizationRuleEngine;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class SanitizationService {

    private final SensitiveWordRepository sensitiveWordRepository;
    private final SanitizationRequestLogRepository requestLogRepository;
    private final SanitizationRuleEngine sanitizationRuleEngine;

    public SanitizationService(
            SensitiveWordRepository sensitiveWordRepository,
            SanitizationRequestLogRepository requestLogRepository,
            SanitizationRuleEngine sanitizationRuleEngine
    ) {
        this.sensitiveWordRepository = sensitiveWordRepository;
        this.requestLogRepository = requestLogRepository;
        this.sanitizationRuleEngine = sanitizationRuleEngine;
    }

    @Retryable(
            retryFor = {
                    TransientDataAccessException.class,
                    CannotAcquireLockException.class,
                    QueryTimeoutException.class,
                    CannotCreateTransactionException.class
            },
            maxAttemptsExpression = "${sensitive-words.retry.max-attempts:3}",
            backoff = @Backoff(delayExpression = "${sensitive-words.retry.backoff-ms:150}")
    )
    public SanitizeTextResponse sanitize(SanitizeTextRequest request) {
        long startNanos = System.nanoTime();
        String originalText = request.inputText();

        List<SensitiveWord> activeRules = sensitiveWordRepository.findActiveRules(LocalDateTime.now(ZoneOffset.UTC));
        SanitizationRuleEngine.SanitizationResult result = sanitizationRuleEngine.sanitize(originalText, activeRules);
        String sanitizedText = result.sanitizedText();

        int totalMatches = result.matchedWords().stream()
                .mapToInt(match -> match.replacementCount())
                .sum();

        int processingTimeMs = Math.toIntExact((System.nanoTime() - startNanos) / 1_000_000);

        UUID requestId = null;
        if (request.shouldPersistRequest()) {
            SanitizationRequestLog log = new SanitizationRequestLog();
            log.setSourceSystem(request.sourceSystem());
            log.setOriginalText(originalText);
            log.setSanitizedText(sanitizedText);
            log.setMatchedWordsCount(totalMatches);
            log.setProcessingTimeMs(processingTimeMs);
            SanitizationRequestLog saved = requestLogRepository.save(log);
            requestId = saved.getRequestId();
        }

        return new SanitizeTextResponse(
                requestId,
                originalText,
                sanitizedText,
                totalMatches,
                processingTimeMs,
                result.matchedWords()
        );
    }
}
