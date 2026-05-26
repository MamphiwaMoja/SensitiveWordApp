package za.co.assessment.sensitivewords.web.rest;

import brave.Tracer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import za.co.assessment.sensitivewords.dto.request.CreateSensitiveWordRequest;
import za.co.assessment.sensitivewords.dto.response.SensitiveWordResponse;
import za.co.assessment.sensitivewords.service.SensitiveWordService;
import za.co.assessment.sensitivewords.web.rest.errors.DuplicateSensitiveWordException;
import za.co.assessment.sensitivewords.web.rest.errors.ExceptionTranslator;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = SensitiveWordResource.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ExceptionTranslator.class)
class SensitiveWordResourceTest {

    private static final String SENSITIVE_WORDS_PATH = "/api/v1/sensitive-words";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SensitiveWordService sensitiveWordService;

    @MockBean
    private Tracer tracer;

    @Test
    void create_shouldReturnCreatedAndLocationHeader() throws Exception {
        when(sensitiveWordService.create(any(CreateSensitiveWordRequest.class)))
                .thenReturn(response(42L, "local-demo-term"));

        mockMvc.perform(post(SENSITIVE_WORDS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "word": "local-demo-term",
                                  "severityLevel": 2
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost" + SENSITIVE_WORDS_PATH + "/42"))
                .andExpect(jsonPath("$.id").value(42));
    }

    @Test
    void create_shouldReturnConflict_whenServiceRejectsDuplicateRule() throws Exception {
        when(sensitiveWordService.create(any(CreateSensitiveWordRequest.class)))
                .thenThrow(new DuplicateSensitiveWordException("duplicate"));

        mockMvc.perform(post(SENSITIVE_WORDS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "word": "duplicate",
                                  "severityLevel": 2
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("duplicate"));
    }

    @Test
    void update_shouldRejectInvalidPathVariable() throws Exception {
        mockMvc.perform(patch(SENSITIVE_WORDS_PATH + "/0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "severityLevel": 2
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void delete_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete(SENSITIVE_WORDS_PATH + "/42"))
                .andExpect(status().isNoContent());

        verify(sensitiveWordService).delete(42L);
    }

    @Test
    void create_shouldReturnBadRequest_whenWordIsMissing() throws Exception {
        mockMvc.perform(post(SENSITIVE_WORDS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "severityLevel": 2
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details.word").value("word is required"));
    }

    @Test
    void findById_shouldReturnBadRequest_whenPathVariableTypeIsInvalid() throws Exception {
        mockMvc.perform(get(SENSITIVE_WORDS_PATH + "/not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value 'not-a-number' for parameter 'id'"))
                .andExpect(jsonPath("$.details.parameter").value("id"))
                .andExpect(jsonPath("$.details.rejectedValue").value("not-a-number"));
    }

    private SensitiveWordResponse response(Long id, String word) {
        return new SensitiveWordResponse(
                id,
                word,
                word,
                2,
                LocalDateTime.of(2026, 5, 23, 12, 0),
                LocalDateTime.of(2026, 5, 23, 12, 0),
                null
        );
    }
}
