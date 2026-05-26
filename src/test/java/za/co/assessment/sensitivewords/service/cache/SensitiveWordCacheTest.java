package za.co.assessment.sensitivewords.service.cache;

import org.junit.jupiter.api.Test;
import za.co.assessment.sensitivewords.config.ApplicationProperties;
import za.co.assessment.sensitivewords.domain.SensitiveWord;
import za.co.assessment.sensitivewords.repository.SensitiveWordRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SensitiveWordCacheTest {

    private final SensitiveWordRepository repository = mock(SensitiveWordRepository.class);
    private final ApplicationProperties properties = new ApplicationProperties();
    private final SensitiveWordCache cache = new SensitiveWordCache(repository, properties);

    @Test
    void getWords_shouldLoadFromDatabaseOnceAndReuseSnapshot() {
        when(repository.findWordsForSanitization()).thenReturn(List.of(word(1L, "scam", 5)));

        List<CachedSensitiveWord> firstRead = cache.getWords();
        List<CachedSensitiveWord> secondRead = cache.getWords();

        assertThat(firstRead).containsExactly(new CachedSensitiveWord(1L, "scam", 5));
        assertThat(secondRead).isSameAs(firstRead);
        verify(repository, times(1)).findWordsForSanitization();
    }

    @Test
    void invalidate_shouldForceNextReadToReload() {
        when(repository.findWordsForSanitization())
                .thenReturn(List.of(word(1L, "scam", 5)))
                .thenReturn(List.of(word(2L, "blocked", 4)));

        cache.getWords();
        cache.invalidate();
        List<CachedSensitiveWord> reloaded = cache.getWords();

        assertThat(reloaded).containsExactly(new CachedSensitiveWord(2L, "blocked", 4));
        verify(repository, times(2)).findWordsForSanitization();
    }

    @Test
    void refresh_shouldKeepExistingSnapshot_whenReloadFailsAfterInitialLoad() {
        when(repository.findWordsForSanitization())
                .thenReturn(List.of(word(1L, "scam", 5)))
                .thenThrow(new IllegalStateException("database unavailable"));

        List<CachedSensitiveWord> initialSnapshot = cache.getWords();
        List<CachedSensitiveWord> fallbackSnapshot = cache.refresh();

        assertThat(fallbackSnapshot).isSameAs(initialSnapshot);
        assertThat(fallbackSnapshot).containsExactly(new CachedSensitiveWord(1L, "scam", 5));
    }

    @Test
    void getWords_shouldKeepPreviousSnapshot_whenReloadFailsAfterInvalidation() {
        when(repository.findWordsForSanitization())
                .thenReturn(List.of(word(1L, "scam", 5)))
                .thenThrow(new IllegalStateException("database unavailable"));

        List<CachedSensitiveWord> initialSnapshot = cache.getWords();
        cache.invalidate();
        List<CachedSensitiveWord> fallbackSnapshot = cache.getWords();

        assertThat(fallbackSnapshot).isSameAs(initialSnapshot);
        assertThat(fallbackSnapshot).containsExactly(new CachedSensitiveWord(1L, "scam", 5));
    }

    @Test
    void refresh_shouldPropagateException_whenInitialLoadFails() {
        when(repository.findWordsForSanitization()).thenThrow(new IllegalStateException("database unavailable"));

        assertThatThrownBy(cache::getWords)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("database unavailable");
    }

    @Test
    void scheduledRefresh_shouldDoNothing_whenDisabled() {
        properties.getCache().getWords().setScheduledRefreshEnabled(false);

        cache.scheduledRefresh();

        verify(repository, times(0)).findWordsForSanitization();
    }

    private SensitiveWord word(Long id, String value, Integer severityLevel) {
        SensitiveWord word = new SensitiveWord();
        word.setId(id);
        word.setWord(value);
        word.setSeverityLevel(severityLevel);
        return word;
    }
}
