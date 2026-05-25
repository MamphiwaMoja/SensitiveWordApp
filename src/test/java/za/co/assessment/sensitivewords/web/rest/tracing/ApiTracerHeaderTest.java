package za.co.assessment.sensitivewords.web.rest.tracing;

import brave.Span;
import brave.Tracer;
import brave.propagation.TraceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import za.co.assessment.sensitivewords.web.rest.HealthResource;
import za.co.assessment.sensitivewords.web.rest.errors.ExceptionTranslator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = HealthResource.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ExceptionTranslator.class)
class ApiTracerHeaderTest {

    private static final String TRACE_ID = "000000000000007b";
    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String HEALTH_PATH = "/api/v1/health";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private Tracer tracer;

    @BeforeEach
    void setUpTracer() {
        TraceContext context = TraceContext.newBuilder()
                .traceId(123L)
                .spanId(456L)
                .build();
        Span span = mock(Span.class);
        when(span.context()).thenReturn(context);
        when(tracer.currentSpan()).thenReturn(span);
    }

    @Test
    void shouldReturnTraceIdHeaderFromCurrentBraveSpan() throws Exception {
        mockMvc.perform(get(HEALTH_PATH))
                .andExpect(status().isOk())
                .andExpect(header().string(TRACE_ID_HEADER, TRACE_ID));
    }
}
