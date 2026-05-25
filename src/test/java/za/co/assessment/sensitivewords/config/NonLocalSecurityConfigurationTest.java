package za.co.assessment.sensitivewords.config;

import brave.Tracer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import za.co.assessment.sensitivewords.service.SensitiveWordService;
import za.co.assessment.sensitivewords.web.rest.HealthResource;
import za.co.assessment.sensitivewords.web.rest.SensitiveWordResource;
import za.co.assessment.sensitivewords.web.rest.errors.ExceptionTranslator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({SensitiveWordResource.class, HealthResource.class})
@Import({SecurityConfiguration.class, ExceptionTranslator.class})
@ActiveProfiles(Constants.SPRING_PROFILE_PROD)
@TestPropertySource(properties = {
        "sensitive-words.security.basic.password=test-password"
})
class NonLocalSecurityConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SensitiveWordService sensitiveWordService;

    @MockBean
    private Tracer tracer;

    @Test
    void nonLocalProfile_shouldRejectApiRequestsWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/sensitive-words"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void nonLocalProfile_shouldPermitApiRequestsWithBasicAuthentication() throws Exception {
        when(sensitiveWordService.findAll(any(Pageable.class))).thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/sensitive-words")
                        .with(httpBasic("sensitive-words", "test-password")))
                .andExpect(status().isOk());
    }

    @Test
    void nonLocalProfile_shouldPermitHealthWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk());
    }
}
