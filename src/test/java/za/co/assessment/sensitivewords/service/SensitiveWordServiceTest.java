package za.co.assessment.sensitivewords.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.assessment.sensitivewords.domain.SensitiveWord;
import za.co.assessment.sensitivewords.dto.request.CreateSensitiveWordRequest;
import za.co.assessment.sensitivewords.dto.request.UpdateSensitiveWordRequest;
import za.co.assessment.sensitivewords.mapper.SensitiveWordMapper;
import za.co.assessment.sensitivewords.repository.SensitiveWordCategoryRepository;
import za.co.assessment.sensitivewords.repository.SensitiveWordRepository;
import za.co.assessment.sensitivewords.service.Impl.SensitiveWordServiceImpl;
import za.co.assessment.sensitivewords.service.audit.SensitiveWordAuditService;
import za.co.assessment.sensitivewords.service.cache.ActiveSensitiveWordCache;
import za.co.assessment.sensitivewords.web.rest.errors.DuplicateSensitiveWordException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SensitiveWordServiceTest {

    @Mock
    private SensitiveWordRepository sensitiveWordRepository;

    @Mock
    private SensitiveWordCategoryRepository categoryRepository;

    @Mock
    private SensitiveWordAuditService auditService;

    @Mock
    private ActiveSensitiveWordCache activeSensitiveWordCache;

    @Mock
    private SensitiveWordMapper mapper;

    @InjectMocks
    private SensitiveWordServiceImpl sensitiveWordService;

    @Test
    void create_shouldRejectDuplicateActiveWord() {
        when(sensitiveWordRepository.existsActiveWord("duplicate")).thenReturn(true);

        CreateSensitiveWordRequest request = new CreateSensitiveWordRequest(
                null,
                "Duplicate",
                1,
                true
        );

        assertThatThrownBy(() -> sensitiveWordService.create(request))
                .isInstanceOf(DuplicateSensitiveWordException.class);
    }

    @Test
    void create_shouldSaveWord_whenRequestIsValid() {
        when(sensitiveWordRepository.existsActiveWord("local-term")).thenReturn(false);
        when(sensitiveWordRepository.save(any(SensitiveWord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateSensitiveWordRequest request = new CreateSensitiveWordRequest(
                null,
                "local-term",
                2,
                true
        );

        sensitiveWordService.create(request);

        verify(sensitiveWordRepository).existsActiveWord("local-term");
        verify(sensitiveWordRepository).save(any(SensitiveWord.class));
        verify(auditService).recordInsert(any(SensitiveWord.class));
        verify(activeSensitiveWordCache).invalidate();
        verify(mapper).toResponse(any(SensitiveWord.class));
    }

    @Test
    void update_shouldRejectDuplicateActiveWord() {
        SensitiveWord existing = existingWord(10L, "existing", true);
        when(sensitiveWordRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(sensitiveWordRepository.existsActiveWordExcludingId("duplicate", 10L)).thenReturn(true);

        UpdateSensitiveWordRequest request = new UpdateSensitiveWordRequest(
                null,
                "duplicate",
                null,
                true
        );

        assertThatThrownBy(() -> sensitiveWordService.update(10L, request))
                .isInstanceOf(DuplicateSensitiveWordException.class);
    }

    @Test
    void update_shouldApplyPatchValuesAndAudit() {
        SensitiveWord existing = existingWord(11L, "scam", true);
        when(sensitiveWordRepository.findById(11L)).thenReturn(Optional.of(existing));
        when(sensitiveWordRepository.save(any(SensitiveWord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateSensitiveWordRequest request = new UpdateSensitiveWordRequest(
                null,
                null,
                5,
                false
        );

        sensitiveWordService.update(11L, request);

        ArgumentCaptor<SensitiveWord> captor = ArgumentCaptor.forClass(SensitiveWord.class);
        verify(sensitiveWordRepository).save(captor.capture());
        SensitiveWord saved = captor.getValue();
        assertThat(saved.getSeverityLevel()).isEqualTo(5);
        assertThat(saved.getActive()).isFalse();
        verify(auditService).recordUpdate(any(SensitiveWord.class), any());
        verify(activeSensitiveWordCache).invalidate();
    }

    @Test
    void deactivate_shouldMarkWordInactive() {
        SensitiveWord existing = existingWord(12L, "term", true);
        when(sensitiveWordRepository.findById(12L)).thenReturn(Optional.of(existing));
        when(sensitiveWordRepository.save(any(SensitiveWord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        sensitiveWordService.deactivate(12L);

        ArgumentCaptor<SensitiveWord> captor = ArgumentCaptor.forClass(SensitiveWord.class);
        verify(sensitiveWordRepository).save(captor.capture());
        SensitiveWord saved = captor.getValue();
        assertThat(saved.getActive()).isFalse();
        verify(auditService).recordDeactivate(any(SensitiveWord.class), any());
        verify(activeSensitiveWordCache).invalidate();
    }

    private SensitiveWord existingWord(Long id, String value, boolean active) {
        SensitiveWord sensitiveWord = new SensitiveWord();
        sensitiveWord.setId(id);
        sensitiveWord.setWord(value);
        sensitiveWord.setSeverityLevel(2);
        sensitiveWord.setActive(active);
        sensitiveWord.setCreatedAt(LocalDateTime.now());
        return sensitiveWord;
    }
}
