package za.co.assessment.sensitivewords.service.Impl;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.annotation.Transactional;
import za.co.assessment.sensitivewords.domain.SanitizationRequestLog;
import za.co.assessment.sensitivewords.dto.request.SanitizeTextRequest;
import za.co.assessment.sensitivewords.dto.response.MatchedWordResponse;
import za.co.assessment.sensitivewords.dto.response.SanitizeTextResponse;
import za.co.assessment.sensitivewords.repository.SanitizationRequestLogRepository;
import za.co.assessment.sensitivewords.service.SanitizationService;
import za.co.assessment.sensitivewords.config.Constants;
import za.co.assessment.sensitivewords.service.cache.ActiveSensitiveWord;
import za.co.assessment.sensitivewords.service.cache.ActiveSensitiveWordCache;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SanitizationServiceImpl implements SanitizationService {
    private static final Logger log = LoggerFactory.getLogger(SanitizationServiceImpl.class);

    private final ActiveSensitiveWordCache activeSensitiveWordCache;
    private final SanitizationRequestLogRepository requestLogRepository;

    public SanitizationServiceImpl(
            ActiveSensitiveWordCache activeSensitiveWordCache,
            SanitizationRequestLogRepository requestLogRepository
    ) {
        this.activeSensitiveWordCache = activeSensitiveWordCache;
        this.requestLogRepository = requestLogRepository;
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
    @Override
    @CircuitBreaker(name = "sanitizationService")
    @Transactional(timeoutString = "${sensitive-words.timeouts.sanitize-transaction-seconds:10}")
    public SanitizeTextResponse sanitize(SanitizeTextRequest request) {
        long startNanos = System.nanoTime();
        String originalText = request.inputText();

        List<ActiveSensitiveWord> activeWords = activeSensitiveWordCache.getActiveWords();
        SanitizationResult result = sanitizeText(originalText, activeWords);
        String sanitizedText = result.sanitizedText();

        int totalMatches = result.matchedWords().stream()
                .mapToInt(match -> match.replacementCount())
                .sum();

        int processingTimeMs = Math.toIntExact((System.nanoTime() - startNanos) / 1_000_000);

        UUID requestId = null;
        if (request.shouldPersistRequest()) {
            // Persist bodies only when explicitly requested because the payload can contain sensitive content.
            SanitizationRequestLog requestLog = new SanitizationRequestLog();
            requestLog.setSourceSystem(request.sourceSystem());
            requestLog.setOriginalText(originalText);
            requestLog.setSanitizedText(sanitizedText);
            requestLog.setMatchedWordsCount(totalMatches);
            requestLog.setProcessingTimeMs(processingTimeMs);
            SanitizationRequestLog saved = requestLogRepository.save(requestLog);
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

    private SanitizationResult sanitizeText(String inputText, List<ActiveSensitiveWord> sensitiveWords) {
        String sanitizedText = inputText;
        List<MatchedWordResponse> matchedWords = new ArrayList<>();
        log.debug("Sanitizing text against {} active sensitive words", sensitiveWords.size());
        for (ActiveSensitiveWord sensitiveWord : sensitiveWords) {
            String word = sensitiveWord.word();
            if (word == null || word.isBlank()) {
                continue;
            }

            ReplacementResult result = replaceLiteralWord(sanitizedText, word);
            sanitizedText = result.text();

            if (result.replacementCount() > 0) {
                matchedWords.add(new MatchedWordResponse(
                        sensitiveWord.id(),
                        word,
                        sensitiveWord.severityLevel(),
                        result.replacementCount()
                ));
            }
        }

        return new SanitizationResult(sanitizedText, matchedWords);
    }

    private ReplacementResult replaceLiteralWord(String inputText, String word) {
        Pattern pattern = Pattern.compile(exactWordPattern(word), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        Matcher matcher = pattern.matcher(inputText);
        StringBuffer sanitizedText = new StringBuffer();
        int replacementCount = 0;

        while (matcher.find()) {
            replacementCount++;
            matcher.appendReplacement(sanitizedText, Matcher.quoteReplacement(Constants.DEFAULT_REPLACEMENT));
        }
        matcher.appendTail(sanitizedText);

        return new ReplacementResult(sanitizedText.toString(), replacementCount);
    }

    private String exactWordPattern(String word) {
        return "(?<![\\p{L}\\p{N}])" + Pattern.quote(word) + "(?![\\p{L}\\p{N}])";
    }

    private record SanitizationResult(String sanitizedText, List<MatchedWordResponse> matchedWords) {
    }

    private record ReplacementResult(String text, int replacementCount) {
    }
}
