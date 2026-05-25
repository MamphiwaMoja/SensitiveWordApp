package za.co.assessment.sensitivewords.web.rest;

import brave.Span;
import brave.Tracer;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;
import za.co.assessment.sensitivewords.config.Constants;

@RestController
@RequestMapping("/api/v1/health")
public class HealthResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthResource.class);
    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    private final Tracer tracer;

    public HealthResource(Tracer tracer) {
        this.tracer = tracer;
    }

    @GetMapping
    @Operation(summary = "Application health check")
    public ResponseEntity<Map<String, Object>> health() {
        long startTime = System.currentTimeMillis();
        LOGGER.info("REST request to check application health");

        Map<String, Object> response = Map.of(
                "status", "UP",
                "service", Constants.APPLICATION_NAME,
                "checkedAt", Instant.now().toString()
        );

        LOGGER.info("Application health check completed with status={}. Duration: {} ms", response.get("status"), System.currentTimeMillis() - startTime);

        return withTraceHeader(ResponseEntity.ok()).body(response);
    }

    private ResponseEntity.BodyBuilder withTraceHeader(ResponseEntity.BodyBuilder builder) {
        // Health responses include the trace id so infrastructure probes can be tied back to logs.
        String traceId = currentTraceId();
        if (traceId != null) {
            builder.header(TRACE_ID_HEADER, traceId);
        }
        return builder;
    }

    private String currentTraceId() {
        Span span = tracer.currentSpan();
        return span == null ? null : span.context().traceIdString();
    }
}
