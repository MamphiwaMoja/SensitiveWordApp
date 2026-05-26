package za.co.assessment.sensitivewords.service.cache;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import za.co.assessment.sensitivewords.config.ApplicationProperties;
import za.co.assessment.sensitivewords.domain.SensitiveWord;
import za.co.assessment.sensitivewords.repository.SensitiveWordRepository;

import java.util.List;

@Service
public class SensitiveWordCache {

    private static final Logger log = LoggerFactory.getLogger(SensitiveWordCache.class);

    private final SensitiveWordRepository sensitiveWordRepository;
    private final ApplicationProperties properties;

    private volatile List<CachedSensitiveWord> cachedWords = List.of();
    private volatile boolean initialized;
    private volatile boolean hasSnapshot;

    public SensitiveWordCache(
            SensitiveWordRepository sensitiveWordRepository,
            ApplicationProperties properties
    ) {
        this.sensitiveWordRepository = sensitiveWordRepository;
        this.properties = properties;
    }

    @CircuitBreaker(name = "sensitiveWordCache")
    public List<CachedSensitiveWord> getWords() {
        if (initialized) {
            return cachedWords;
        }
        return refresh();
    }

    public void invalidate() {
        initialized = false;
        log.debug("Sensitive-word cache invalidated");
    }

    @Scheduled(fixedDelayString = "${sensitive-words.cache.words.refresh-interval-ms:300000}")
    public void scheduledRefresh() {
        if (!properties.getCache().getWords().isScheduledRefreshEnabled()) {
            return;
        }
        refresh();
    }

    public synchronized List<CachedSensitiveWord> refresh() {
        try {
            List<CachedSensitiveWord> loadedWords = sensitiveWordRepository.findWordsForSanitization()
                    .stream()
                    .filter(word -> word.getWord() != null && !word.getWord().isBlank())
                    .map(this::toCachedWord)
                    .toList();

            cachedWords = loadedWords;
            initialized = true;
            hasSnapshot = true;
            log.debug("Sensitive-word cache refreshed with {} words", loadedWords.size());
            return loadedWords;
        } catch (RuntimeException ex) {
            if (hasSnapshot) {
                log.warn(
                        "Sensitive-word cache refresh failed; continuing with {} cached words. Cause: {}: {}",
                        cachedWords.size(),
                        ex.getClass().getSimpleName(),
                        ex.getMessage()
                );
                return cachedWords;
            }
            log.error(
                    "Sensitive-word cache initial load failed. Cause: {}: {}",
                    ex.getClass().getSimpleName(),
                    ex.getMessage()
            );
            throw ex;
        }
    }

    private CachedSensitiveWord toCachedWord(SensitiveWord word) {
        return new CachedSensitiveWord(
                word.getId(),
                word.getWord(),
                word.getSeverityLevel() == null ? 1 : word.getSeverityLevel()
        );
    }
}
