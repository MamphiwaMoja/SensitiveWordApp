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

class ActiveSensitiveWordCacheTest {

    private final SensitiveWordRepository repository = mock(SensitiveWordRepository.class);
    private final ApplicationProperties properties = new ApplicationProperties();
    private final ActiveSensitiveWordCache cache = new ActiveSensitiveWordCache(repository, properties);

    @Test
    void getActiveWords_shouldLoadFromDatabaseOnceAndReuseSnapshot() {
        when(repository.findActiveWords()).thenReturn(List.of(word(1L, "scam", 5)));

        List<ActiveSensitiveWord> firstRead = cache.getActiveWords();
        List<ActiveSensitiveWord> secondRead = cache.getActiveWords();

        assertThat(firstRead).containsExactly(new ActiveSensitiveWord(1L, "scam", 5));
        assertThat(secondRead).isSameAs(firstRead);
        verify(repository, times(1)).findActiveWords();
    }

    @Test
    void invalidate_shouldForceNextReadToReload() {
        when(repository.findActiveWords())
                .thenReturn(List.of(word(1L, "scam", 5)))
                .thenReturn(List.of(word(2L, "blocked", 4)));

        cache.getActiveWords();
        cache.invalidate();
        List<ActiveSensitiveWord> reloaded = cache.getActiveWords();

        assertThat(reloaded).containsExactly(new ActiveSensitiveWord(2L, "blocked", 4));
        verify(repository, times(2)).findActiveWords();
    }

    @Test
    void refresh_shouldKeepExistingSnapshot_whenReloadFailsAfterInitialLoad() {
        when(repository.findActiveWords())
                .thenReturn(List.of(word(1L, "scam", 5)))
                .thenThrow(new IllegalStateException("database unavailable"));

        List<ActiveSensitiveWord> initialSnapshot = cache.getActiveWords();
        List<ActiveSensitiveWord> fallbackSnapshot = cache.refresh();

        assertThat(fallbackSnapshot).isSameAs(initialSnapshot);
        assertThat(fallbackSnapshot).containsExactly(new ActiveSensitiveWord(1L, "scam", 5));
    }

    @Test
    void getActiveWords_shouldKeepPreviousSnapshot_whenReloadFailsAfterInvalidation() {
        when(repository.findActiveWords())
                .thenReturn(List.of(word(1L, "scam", 5)))
                .thenThrow(new IllegalStateException("database unavailable"));

        List<ActiveSensitiveWord> initialSnapshot = cache.getActiveWords();
        cache.invalidate();
        List<ActiveSensitiveWord> fallbackSnapshot = cache.getActiveWords();

        assertThat(fallbackSnapshot).isSameAs(initialSnapshot);
        assertThat(fallbackSnapshot).containsExactly(new ActiveSensitiveWord(1L, "scam", 5));
    }

    @Test
    void refresh_shouldPropagateException_whenInitialLoadFails() {
        when(repository.findActiveWords()).thenThrow(new IllegalStateException("database unavailable"));

        assertThatThrownBy(cache::getActiveWords)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("database unavailable");
    }

    @Test
    void scheduledRefresh_shouldDoNothing_whenDisabled() {
        properties.getCache().getActiveWords().setScheduledRefreshEnabled(false);

        cache.scheduledRefresh();

        verify(repository, times(0)).findActiveWords();
    }

    private SensitiveWord word(Long id, String value, Integer severityLevel) {
        SensitiveWord word = new SensitiveWord();
        word.setId(id);
        word.setWord(value);
        word.setSeverityLevel(severityLevel);
        word.setActive(true);
        return word;
    }
}
