package za.co.assessment.sensitivewords.web.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import za.co.assessment.sensitivewords.config.Constants;
import za.co.assessment.sensitivewords.domain.MatchType;
import za.co.assessment.sensitivewords.dto.request.CreateSensitiveWordRequest;
import za.co.assessment.sensitivewords.dto.response.SensitiveWordResponse;
import za.co.assessment.sensitivewords.service.SensitiveWordService;
import za.co.assessment.sensitivewords.web.rest.errors.DuplicateSensitiveWordException;
import za.co.assessment.sensitivewords.web.rest.errors.ExceptionTranslator;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SensitiveWordResource.class)
@Import(ExceptionTranslator.class)
class SensitiveWordResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SensitiveWordService sensitiveWordService;

    @Test
    void create_shouldReturnCreatedAndLocationHeader() throws Exception {
        when(sensitiveWordService.create(any(CreateSensitiveWordRequest.class)))
                .thenReturn(response(42L, "local-demo-term"));

        mockMvc.perform(post(ApiPaths.SENSITIVE_WORDS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "word": "local-demo-term",
                                  "replacementValue": "[removed]",
                                  "matchType": "CONTAINS",
                                  "severityLevel": 2,
                                  "caseSensitive": false,
                                  "active": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost" + ApiPaths.SENSITIVE_WORDS + "/42"))
                .andExpect(jsonPath("$.id").value(42));
    }

    @Test
    void create_shouldReturnConflict_whenServiceRejectsDuplicateRule() throws Exception {
        when(sensitiveWordService.create(any(CreateSensitiveWordRequest.class)))
                .thenThrow(new DuplicateSensitiveWordException("duplicate"));

        mockMvc.perform(post(ApiPaths.SENSITIVE_WORDS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "word": "duplicate",
                                  "replacementValue": "%s",
                                  "matchType": "CONTAINS",
                                  "severityLevel": 2
                                }
                                """.formatted(Constants.DEFAULT_REPLACEMENT)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("duplicate"));
    }

    @Test
    void update_shouldRejectInvalidPathVariable() throws Exception {
        mockMvc.perform(patch(ApiPaths.SENSITIVE_WORDS + "/0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "replacementValue": "%s"
                                }
                                """.formatted(Constants.DEFAULT_REPLACEMENT)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void create_shouldReturnBadRequest_whenMatchTypeIsInvalid() throws Exception {
        mockMvc.perform(post(ApiPaths.SENSITIVE_WORDS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "word": "local-demo-term",
                                  "replacementValue": "[removed]",
                                  "matchType": "INVALID",
                                  "severityLevel": 2
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value 'INVALID' for field 'matchType'"))
                .andExpect(jsonPath("$.details.field").value("matchType"))
                .andExpect(jsonPath("$.details.rejectedValue").value("INVALID"));
    }

    @Test
    void findById_shouldReturnBadRequest_whenPathVariableTypeIsInvalid() throws Exception {
        mockMvc.perform(get(ApiPaths.SENSITIVE_WORDS + "/not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value 'not-a-number' for parameter 'id'"))
                .andExpect(jsonPath("$.details.parameter").value("id"))
                .andExpect(jsonPath("$.details.rejectedValue").value("not-a-number"));
    }

    private SensitiveWordResponse response(Long id, String word) {
        return new SensitiveWordResponse(
                id,
                1L,
                "PROFANITY",
                "Profanity",
                word,
                word,
                Constants.DEFAULT_REPLACEMENT,
                MatchType.CONTAINS,
                2,
                false,
                true,
                LocalDateTime.of(2026, 5, 23, 12, 0),
                null,
                "test",
                LocalDateTime.of(2026, 5, 23, 12, 0),
                null
        );
    }
}
