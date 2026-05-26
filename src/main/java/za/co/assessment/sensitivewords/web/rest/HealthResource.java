package za.co.assessment.sensitivewords.web.rest;

import brave.Span;
import brave.Tracer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import za.co.assessment.sensitivewords.config.Constants;
import za.co.assessment.sensitivewords.dto.response.HealthResponse;

@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health", description = "Application health endpoints")
public class HealthResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthResource.class);
    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    private final Tracer tracer;

    public HealthResource(Tracer tracer) {
        this.tracer = tracer;
    }

    @GetMapping
    @Operation(summary = "Application health check", description = "Returns a lightweight API health response for probes and smoke tests.")
    @ApiResponse(
            responseCode = "200",
            description = "Application is healthy",
            headers = @Header(name = TRACE_ID_HEADER, description = "Trace id for log correlation."),
            content = @Content(schema = @Schema(implementation = HealthResponse.class))
    )
    public ResponseEntity<HealthResponse> health() {
        long startTime = System.currentTimeMillis();
        LOGGER.info("REST request to check application health");

        HealthResponse response = new HealthResponse(
                "UP",
                Constants.APPLICATION_NAME,
                Instant.now().toString()
        );

        LOGGER.info("Application health check completed with status={}. Duration: {} ms", response.status(), System.currentTimeMillis() - startTime);

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
