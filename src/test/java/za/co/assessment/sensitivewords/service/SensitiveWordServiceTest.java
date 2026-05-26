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
import za.co.assessment.sensitivewords.repository.SensitiveWordRepository;
import za.co.assessment.sensitivewords.service.impl.SensitiveWordServiceImpl;
import za.co.assessment.sensitivewords.service.audit.SensitiveWordAuditService;
import za.co.assessment.sensitivewords.service.cache.SensitiveWordCache;
import za.co.assessment.sensitivewords.web.rest.errors.DuplicateSensitiveWordException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SensitiveWordServiceTest {

    @Mock
    private SensitiveWordRepository sensitiveWordRepository;

    @Mock
    private SensitiveWordAuditService auditService;

    @Mock
    private SensitiveWordCache sensitiveWordCache;

    @Mock
    private SensitiveWordMapper mapper;

    @InjectMocks
    private SensitiveWordServiceImpl sensitiveWordService;

    @Test
    void create_shouldRejectDuplicateWord() {
        when(sensitiveWordRepository.existsByNormalizedWord("duplicate")).thenReturn(true);

        CreateSensitiveWordRequest request = new CreateSensitiveWordRequest(
                "Duplicate",
                1
        );

        assertThatThrownBy(() -> sensitiveWordService.create(request))
                .isInstanceOf(DuplicateSensitiveWordException.class);
    }

    @Test
    void create_shouldSaveWord_whenRequestIsValid() {
        when(sensitiveWordRepository.existsByNormalizedWord("local-term")).thenReturn(false);
        when(sensitiveWordRepository.save(any(SensitiveWord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateSensitiveWordRequest request = new CreateSensitiveWordRequest(
                "local-term",
                2
        );

        sensitiveWordService.create(request);

        verify(sensitiveWordRepository).existsByNormalizedWord("local-term");
        verify(sensitiveWordRepository).save(any(SensitiveWord.class));
        verify(auditService).recordInsert(any(SensitiveWord.class));
        verify(sensitiveWordCache).invalidate();
        verify(mapper).toResponse(any(SensitiveWord.class));
    }

    @Test
    void update_shouldRejectDuplicateWord() {
        SensitiveWord existing = existingWord(10L, "existing");
        when(sensitiveWordRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(sensitiveWordRepository.existsByNormalizedWordExcludingId("duplicate", 10L)).thenReturn(true);

        UpdateSensitiveWordRequest request = new UpdateSensitiveWordRequest(
                "duplicate",
                null
        );

        assertThatThrownBy(() -> sensitiveWordService.update(10L, request))
                .isInstanceOf(DuplicateSensitiveWordException.class);
    }

    @Test
    void update_shouldApplyPatchValuesAndAudit() {
        SensitiveWord existing = existingWord(11L, "scam");
        when(sensitiveWordRepository.findById(11L)).thenReturn(Optional.of(existing));
        when(sensitiveWordRepository.save(any(SensitiveWord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateSensitiveWordRequest request = new UpdateSensitiveWordRequest(
                null,
                5
        );

        sensitiveWordService.update(11L, request);

        ArgumentCaptor<SensitiveWord> captor = ArgumentCaptor.forClass(SensitiveWord.class);
        verify(sensitiveWordRepository).save(captor.capture());
        SensitiveWord saved = captor.getValue();
        assertThat(saved.getSeverityLevel()).isEqualTo(5);
        verify(auditService).recordUpdate(any(SensitiveWord.class), any());
        verify(sensitiveWordCache).invalidate();
    }

    @Test
    void delete_shouldHardDeleteWord() {
        SensitiveWord existing = existingWord(12L, "term");
        when(sensitiveWordRepository.findById(12L)).thenReturn(Optional.of(existing));

        sensitiveWordService.delete(12L);

        verify(auditService).recordDelete(existing, null);
        verify(sensitiveWordRepository).delete(existing);
        verify(sensitiveWordRepository, never()).save(existing);
        verify(sensitiveWordCache).invalidate();
    }

    private SensitiveWord existingWord(Long id, String value) {
        SensitiveWord sensitiveWord = new SensitiveWord();
        sensitiveWord.setId(id);
        sensitiveWord.setWord(value);
        sensitiveWord.setSeverityLevel(2);
        sensitiveWord.setCreatedAt(LocalDateTime.now());
        return sensitiveWord;
    }
}
