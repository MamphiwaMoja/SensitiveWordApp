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
import za.co.assessment.sensitivewords.dto.request.SanitizeTextRequest;
import za.co.assessment.sensitivewords.dto.response.SanitizeTextResponse;
import za.co.assessment.sensitivewords.service.SanitizationService;
import za.co.assessment.sensitivewords.web.rest.errors.ErrorMessages;
import za.co.assessment.sensitivewords.web.rest.errors.ExceptionTranslator;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = SanitizationResource.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ExceptionTranslator.class)
class SanitizationResourceTest {

    private static final String SANITIZE_PATH = "/api/v1/sanitize";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SanitizationService sanitizationService;

    @MockBean
    private Tracer tracer;

    @Test
    void sanitize_shouldReturnSanitizedResponse() throws Exception {
        when(sanitizationService.sanitize(any(SanitizeTextRequest.class)))
                .thenReturn(new SanitizeTextResponse(
                        null,
                        "This contains testbadword",
                        "This contains ***",
                        1,
                        2,
                        List.of()
                ));

        mockMvc.perform(post(SANITIZE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "inputText": "This contains testbadword",
                                  "sourceSystem": "mvc-test"
                                }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sanitizedText").value("This contains ***"))
                .andExpect(jsonPath("$.matchedWordsCount").value(1));
    }

    @Test
    void sanitize_shouldReturnBadRequest_whenInputIsBlank() throws Exception {
        mockMvc.perform(post(SANITIZE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "inputText": "  ",
                                  "sourceSystem": "mvc-test"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorMessages.VALIDATION_FAILED))
                .andExpect(jsonPath("$.details.inputText").value("inputText is required"));

        verify(sanitizationService, never()).sanitize(any(SanitizeTextRequest.class));
    }
}
