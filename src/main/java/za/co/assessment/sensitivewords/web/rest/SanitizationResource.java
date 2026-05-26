package za.co.assessment.sensitivewords.web.rest;

import brave.Span;
import brave.Tracer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import za.co.assessment.sensitivewords.dto.request.SanitizeTextRequest;
import za.co.assessment.sensitivewords.dto.response.ErrorResponse;
import za.co.assessment.sensitivewords.dto.response.SanitizeTextResponse;
import za.co.assessment.sensitivewords.service.SanitizationService;
import za.co.assessment.sensitivewords.web.rest.errors.ErrorMessages;

@RestController
@RequestMapping("/api/v1/sanitize")
@Tag(name = "Sanitization", description = "APIs for sanitizing incoming text")
public class SanitizationResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(SanitizationResource.class);
    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    private final Tracer tracer;
    private final SanitizationService sanitizationService;

    public SanitizationResource(Tracer tracer, SanitizationService sanitizationService) {
        this.tracer = tracer;
        this.sanitizationService = sanitizationService;
    }

    @PostMapping
    @Operation(
            summary = "Sanitize input text using configured sensitive words",
            description = "Replaces configured sensitive words from the database with the default mask and returns the sanitized text. "
                    + "Request payload persistence is disabled by default and must be explicitly enabled."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Text sanitized successfully",
                    headers = @Header(name = TRACE_ID_HEADER, description = "Trace id for log correlation."),
                    content = @Content(schema = @Schema(implementation = SanitizeTextResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request payload",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = ErrorMessages.SERVICE_TEMPORARILY_UNAVAILABLE,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "504",
                    description = ErrorMessages.REQUEST_TIMED_OUT,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = ErrorMessages.UNEXPECTED_SERVER_ERROR,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<SanitizeTextResponse> sanitize(@Valid @RequestBody SanitizeTextRequest request) {
        long startTime = System.currentTimeMillis();

        LOGGER.info(
                "REST request to sanitize text from sourceSystem={}, persistRequest={}, inputLength={}",
                request.sourceSystem(),
                request.shouldPersistRequest(),
                request.inputText() == null ? 0 : request.inputText().length()
        );

        SanitizeTextResponse response = sanitizationService.sanitize(request);

        LOGGER.info(
                "Sanitize request completed for sourceSystem={} with matchedWordsCount={}, requestId={}. Duration: {} ms",
                request.sourceSystem(),
                response.matchedWordsCount(),
                response.requestId(),
                System.currentTimeMillis() - startTime
        );

        return withTraceHeader(ResponseEntity.ok()).body(response);
    }

    private ResponseEntity.BodyBuilder withTraceHeader(ResponseEntity.BodyBuilder builder) {

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
