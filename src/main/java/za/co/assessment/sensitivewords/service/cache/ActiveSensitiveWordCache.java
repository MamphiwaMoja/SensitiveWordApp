package za.co.assessment.sensitivewords.service.cache;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.assessment.sensitivewords.config.ApplicationProperties;
import za.co.assessment.sensitivewords.domain.SensitiveWord;
import za.co.assessment.sensitivewords.repository.SensitiveWordRepository;

import java.util.List;

@Service
public class ActiveSensitiveWordCache {

    private static final Logger log = LoggerFactory.getLogger(ActiveSensitiveWordCache.class);

    private final SensitiveWordRepository sensitiveWordRepository;
    private final ApplicationProperties properties;

    private volatile List<ActiveSensitiveWord> cachedWords = List.of();
    private volatile boolean initialized;
    private volatile boolean hasSnapshot;

    public ActiveSensitiveWordCache(
            SensitiveWordRepository sensitiveWordRepository,
            ApplicationProperties properties
    ) {
        this.sensitiveWordRepository = sensitiveWordRepository;
        this.properties = properties;
    }

    @CircuitBreaker(name = "activeSensitiveWordCache")
    @Transactional(readOnly = true, timeoutString = "${sensitive-words.timeouts.read-transaction-seconds:5}")
    public List<ActiveSensitiveWord> getActiveWords() {
        if (initialized) {
            return cachedWords;
        }
        return refresh();
    }

    public void invalidate() {
        initialized = false;
        log.debug("Active sensitive-word cache invalidated");
    }

    @Scheduled(fixedDelayString = "${sensitive-words.cache.active-words.refresh-interval-ms:300000}")
    @CircuitBreaker(name = "activeSensitiveWordCache")
    @Transactional(readOnly = true, timeoutString = "${sensitive-words.timeouts.read-transaction-seconds:5}")
    public void scheduledRefresh() {
        if (!properties.getCache().getActiveWords().isScheduledRefreshEnabled()) {
            return;
        }
        refresh();
    }

    @CircuitBreaker(name = "activeSensitiveWordCache")
    @Transactional(readOnly = true, timeoutString = "${sensitive-words.timeouts.read-transaction-seconds:5}")
    public synchronized List<ActiveSensitiveWord> refresh() {
        try {
            List<ActiveSensitiveWord> loadedWords = sensitiveWordRepository.findActiveWords()
                    .stream()
                    .filter(word -> word.getWord() != null && !word.getWord().isBlank())
                    .map(this::toCachedWord)
                    .toList();

            cachedWords = loadedWords;
            initialized = true;
            hasSnapshot = true;
            log.debug("Active sensitive-word cache refreshed with {} words", loadedWords.size());
            return loadedWords;
        } catch (RuntimeException ex) {
            if (hasSnapshot) {
                log.warn(
                        "Active sensitive-word cache refresh failed; continuing with {} cached words. Cause: {}: {}",
                        cachedWords.size(),
                        ex.getClass().getSimpleName(),
                        ex.getMessage()
                );
                return cachedWords;
            }
            log.error(
                    "Active sensitive-word cache initial load failed. Cause: {}: {}",
                    ex.getClass().getSimpleName(),
                    ex.getMessage()
            );
            throw ex;
        }
    }

    private ActiveSensitiveWord toCachedWord(SensitiveWord word) {
        return new ActiveSensitiveWord(
                word.getId(),
                word.getWord(),
                word.getSeverityLevel() == null ? 1 : word.getSeverityLevel()
        );
    }
}
