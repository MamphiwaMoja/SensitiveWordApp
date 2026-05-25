package za.co.assessment.sensitivewords.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.assessment.sensitivewords.config.Constants;
import za.co.assessment.sensitivewords.domain.MatchType;
import za.co.assessment.sensitivewords.domain.SensitiveWord;
import za.co.assessment.sensitivewords.dto.request.CreateSensitiveWordRequest;
import za.co.assessment.sensitivewords.dto.request.UpdateSensitiveWordRequest;
import za.co.assessment.sensitivewords.mapper.SensitiveWordMapper;
import za.co.assessment.sensitivewords.repository.SensitiveWordCategoryRepository;
import za.co.assessment.sensitivewords.repository.SensitiveWordRepository;
import za.co.assessment.sensitivewords.service.audit.SensitiveWordAuditService;
import za.co.assessment.sensitivewords.service.validation.SensitiveWordDefinitionValidator;
import za.co.assessment.sensitivewords.web.rest.errors.BadRequestException;
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
    private SensitiveWordDefinitionValidator definitionValidator;

    @Mock
    private SensitiveWordMapper mapper;

    @InjectMocks
    private SensitiveWordService sensitiveWordService;

    @Test
    void create_shouldRejectDuplicateActiveRule() {
        when(sensitiveWordRepository.existsActiveRule("duplicate", MatchType.CONTAINS)).thenReturn(true);

        CreateSensitiveWordRequest request = new CreateSensitiveWordRequest(
                null,
                "Duplicate",
                Constants.DEFAULT_REPLACEMENT,
                MatchType.CONTAINS,
                1,
                false,
                true,
                null
        );

        assertThatThrownBy(() -> sensitiveWordService.create(request))
                .isInstanceOf(DuplicateSensitiveWordException.class);
    }

    @Test
    void create_shouldSaveRule_whenRequestIsValid() {
        when(sensitiveWordRepository.existsActiveRule("local-term", MatchType.CONTAINS)).thenReturn(false);
        when(sensitiveWordRepository.save(any(SensitiveWord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateSensitiveWordRequest request = new CreateSensitiveWordRequest(
                null,
                "local-term",
                null,
                MatchType.CONTAINS,
                2,
                false,
                true,
                "test"
        );

        sensitiveWordService.create(request);

        verify(definitionValidator).validate("local-term", MatchType.CONTAINS);
        verify(sensitiveWordRepository).existsActiveRule("local-term", MatchType.CONTAINS);
        verify(sensitiveWordRepository).save(any(SensitiveWord.class));
        verify(auditService).recordInsert(any(SensitiveWord.class));
        verify(mapper).toResponse(any(SensitiveWord.class));
    }

    @Test
    void create_shouldRejectInvalidRegexPattern() {
        CreateSensitiveWordRequest request = new CreateSensitiveWordRequest(
                null,
                "[invalid",
                Constants.DEFAULT_REPLACEMENT,
                MatchType.REGEX,
                1,
                false,
                true,
                null
        );

        org.mockito.Mockito.doThrow(new BadRequestException("Invalid regex pattern: test"))
                .when(definitionValidator)
                .validate("[invalid", MatchType.REGEX);

        assertThatThrownBy(() -> sensitiveWordService.create(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid regex pattern");
    }

    @Test
    void update_shouldRejectInvalidRegexPattern() {
        SensitiveWord existing = existingRule(10L, "existing", MatchType.CONTAINS, true);
        when(sensitiveWordRepository.findById(10L)).thenReturn(Optional.of(existing));

        UpdateSensitiveWordRequest request = new UpdateSensitiveWordRequest(
                null,
                "[invalid",
                null,
                MatchType.REGEX,
                null,
                null,
                null,
                null
        );

        org.mockito.Mockito.doThrow(new BadRequestException("Invalid regex pattern: test"))
                .when(definitionValidator)
                .validate("[invalid", MatchType.REGEX);

        assertThatThrownBy(() -> sensitiveWordService.update(10L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid regex pattern");
    }

    @Test
    void update_shouldApplyPatchValuesAndAudit() {
        SensitiveWord existing = existingRule(11L, "scam", MatchType.CONTAINS, true);
        when(sensitiveWordRepository.findById(11L)).thenReturn(Optional.of(existing));
        when(sensitiveWordRepository.save(any(SensitiveWord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateSensitiveWordRequest request = new UpdateSensitiveWordRequest(
                null,
                null,
                "[risk-term]",
                null,
                5,
                true,
                false,
                "updated"
        );

        sensitiveWordService.update(11L, request);

        ArgumentCaptor<SensitiveWord> captor = ArgumentCaptor.forClass(SensitiveWord.class);
        verify(sensitiveWordRepository).save(captor.capture());
        SensitiveWord saved = captor.getValue();
        assertThat(saved.getReplacementValue()).isEqualTo("[risk-term]");
        assertThat(saved.getSeverityLevel()).isEqualTo(5);
        assertThat(saved.getCaseSensitive()).isTrue();
        assertThat(saved.getActive()).isFalse();
        assertThat(saved.getEffectiveTo()).isNotNull();
        verify(auditService).recordUpdate(any(SensitiveWord.class), any());
    }

    @Test
    void deactivate_shouldMarkRuleInactiveAndSetEffectiveTo() {
        SensitiveWord existing = existingRule(12L, "term", MatchType.CONTAINS, true);
        when(sensitiveWordRepository.findById(12L)).thenReturn(Optional.of(existing));
        when(sensitiveWordRepository.save(any(SensitiveWord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        sensitiveWordService.deactivate(12L);

        ArgumentCaptor<SensitiveWord> captor = ArgumentCaptor.forClass(SensitiveWord.class);
        verify(sensitiveWordRepository).save(captor.capture());
        SensitiveWord saved = captor.getValue();
        assertThat(saved.getActive()).isFalse();
        assertThat(saved.getEffectiveTo()).isNotNull();
        verify(auditService).recordDeactivate(any(SensitiveWord.class), any());
    }

    private SensitiveWord existingRule(Long id, String word, MatchType matchType, boolean active) {
        SensitiveWord rule = new SensitiveWord();
        rule.setId(id);
        rule.setWord(word);
        rule.setReplacementValue(Constants.DEFAULT_REPLACEMENT);
        rule.setMatchType(matchType);
        rule.setSeverityLevel(2);
        rule.setCaseSensitive(false);
        rule.setActive(active);
        rule.setCreatedAt(LocalDateTime.now());
        return rule;
    }
}
