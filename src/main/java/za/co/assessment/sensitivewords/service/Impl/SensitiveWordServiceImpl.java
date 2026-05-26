package za.co.assessment.sensitivewords.service.Impl;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.annotation.Transactional;
import za.co.assessment.sensitivewords.config.Constants;
import za.co.assessment.sensitivewords.domain.SensitiveWord;
import za.co.assessment.sensitivewords.dto.request.CreateSensitiveWordRequest;
import za.co.assessment.sensitivewords.dto.request.UpdateSensitiveWordRequest;
import za.co.assessment.sensitivewords.dto.response.SensitiveWordResponse;
import za.co.assessment.sensitivewords.mapper.SensitiveWordMapper;
import za.co.assessment.sensitivewords.repository.SensitiveWordRepository;
import za.co.assessment.sensitivewords.service.SensitiveWordService;
import za.co.assessment.sensitivewords.service.audit.SensitiveWordAuditService;
import za.co.assessment.sensitivewords.service.cache.ActiveSensitiveWordCache;
import za.co.assessment.sensitivewords.web.rest.errors.BadRequestException;
import za.co.assessment.sensitivewords.web.rest.errors.DuplicateSensitiveWordException;
import za.co.assessment.sensitivewords.web.rest.errors.ResourceNotFoundException;

import java.util.Locale;

@Service
public class SensitiveWordServiceImpl implements SensitiveWordService {

    private final SensitiveWordRepository sensitiveWordRepository;
    private final SensitiveWordMapper mapper;
    private final SensitiveWordAuditService auditService;
    private final ActiveSensitiveWordCache activeSensitiveWordCache;

    public SensitiveWordServiceImpl(
            SensitiveWordRepository sensitiveWordRepository,
            SensitiveWordMapper mapper,
            SensitiveWordAuditService auditService,
            ActiveSensitiveWordCache activeSensitiveWordCache
    ) {
        this.sensitiveWordRepository = sensitiveWordRepository;
        this.mapper = mapper;
        this.auditService = auditService;
        this.activeSensitiveWordCache = activeSensitiveWordCache;
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
    @CircuitBreaker(name = "sensitiveWordService")
    @Transactional(readOnly = true, timeoutString = "${sensitive-words.timeouts.read-transaction-seconds:5}")
    public Page<SensitiveWordResponse> findAll(Pageable pageable) {
        return sensitiveWordRepository.findAll(pageable).map(mapper::toResponse);
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
    @CircuitBreaker(name = "sensitiveWordService")
    @Transactional(readOnly = true, timeoutString = "${sensitive-words.timeouts.read-transaction-seconds:5}")
    public SensitiveWordResponse findById(Long id) {
        return mapper.toResponse(getRequiredSensitiveWord(id));
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
    @CircuitBreaker(name = "sensitiveWordService")
    @Transactional(timeoutString = "${sensitive-words.timeouts.write-transaction-seconds:10}")
    public SensitiveWordResponse create(CreateSensitiveWordRequest request) {
        String normalizedWord = normalize(request.word());
        boolean active = request.active() == null || request.active();

        if (active && sensitiveWordRepository.existsActiveWord(normalizedWord)) {
            throw new DuplicateSensitiveWordException("An active sensitive word already exists for this word");
        }

        SensitiveWord word = new SensitiveWord();
        word.setWord(request.word().trim());
        word.setSeverityLevel(request.severityLevel() == null ? 1 : request.severityLevel());
        word.setActive(active);
        word.setCreatedBy(Constants.SYSTEM_ACCOUNT);

        SensitiveWord saved = sensitiveWordRepository.save(word);
        auditService.recordInsert(saved);
        activeSensitiveWordCache.invalidate();
        return mapper.toResponse(saved);
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
    @CircuitBreaker(name = "sensitiveWordService")
    @Transactional(timeoutString = "${sensitive-words.timeouts.write-transaction-seconds:10}")
    public SensitiveWordResponse update(Long id, UpdateSensitiveWordRequest request) {
        SensitiveWord existing = getRequiredSensitiveWord(id);
        String oldSnapshot = auditService.snapshot(existing);

        String proposedWord = request.word() == null ? existing.getWord() : request.word().trim();
        if (proposedWord.isBlank()) {
            throw new BadRequestException("word must not be blank");
        }

        String normalizedWord = normalize(proposedWord);
        boolean activeAfterUpdate = request.active() == null ? Boolean.TRUE.equals(existing.getActive()) : request.active();

        if (activeAfterUpdate
                && sensitiveWordRepository.existsActiveWordExcludingId(normalizedWord, id)) {
            throw new DuplicateSensitiveWordException("Another active sensitive word already exists for this word");
        }

        if (request.word() != null) {
            existing.setWord(proposedWord);
        }
        if (request.severityLevel() != null) {
            existing.setSeverityLevel(request.severityLevel());
        }
        if (request.active() != null) {
            existing.setActive(request.active());
        }
        existing.setUpdatedBy(Constants.SYSTEM_ACCOUNT);

        SensitiveWord saved = sensitiveWordRepository.save(existing);
        auditService.recordUpdate(saved, oldSnapshot);
        activeSensitiveWordCache.invalidate();
        return mapper.toResponse(saved);
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
    @CircuitBreaker(name = "sensitiveWordService")
    @Transactional(timeoutString = "${sensitive-words.timeouts.write-transaction-seconds:10}")
    public void deactivate(Long id) {
        SensitiveWord existing = getRequiredSensitiveWord(id);
        String oldSnapshot = auditService.snapshot(existing);

        // Deactivation is a soft delete so historical audit records and old references remain intact.
        existing.setActive(false);
        existing.setUpdatedBy(Constants.SYSTEM_ACCOUNT);

        SensitiveWord saved = sensitiveWordRepository.save(existing);
        auditService.recordDeactivate(saved, oldSnapshot);
        activeSensitiveWordCache.invalidate();
    }

    private SensitiveWord getRequiredSensitiveWord(Long id) {
        return sensitiveWordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sensitive word not found for id: " + id));
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
    }

}
