package za.co.assessment.sensitivewords.web.rest;

import brave.Span;
import brave.Tracer;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springdoc.core.annotations.ParameterObject;
import za.co.assessment.sensitivewords.dto.request.CreateSensitiveWordRequest;
import za.co.assessment.sensitivewords.dto.request.UpdateSensitiveWordRequest;
import za.co.assessment.sensitivewords.dto.response.ErrorResponse;
import za.co.assessment.sensitivewords.dto.response.SensitiveWordResponse;
import za.co.assessment.sensitivewords.service.SensitiveWordService;
import za.co.assessment.sensitivewords.web.rest.errors.ErrorMessages;

import java.net.URI;

@RestController
@RequestMapping("/api/v1")
@Validated
@Tag(name = "Sensitive Words", description = "CRUD APIs for sensitive words")
public class SensitiveWordResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(SensitiveWordResource.class);
    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    private final Tracer tracer;
    private final SensitiveWordService sensitiveWordService;

    public SensitiveWordResource(Tracer tracer, SensitiveWordService sensitiveWordService) {
        this.tracer = tracer;
        this.sensitiveWordService = sensitiveWordService;
    }

    @GetMapping("/sensitive-words")
    @Operation(
            summary = "List sensitive words",
            description = "Returns a paginated list of sensitive-word records that have not been deleted."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Sensitive words returned successfully",
                    headers = @Header(name = TRACE_ID_HEADER, description = "Trace id for log correlation."),
                    content = @Content(schema = @Schema(implementation = SensitiveWordResponse.class))
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
            )
    })
    public ResponseEntity<Page<SensitiveWordResponse>> findAll(
            @ParameterObject
            @PageableDefault(size = 20, sort = "id") Pageable pageable
    ) {
        long startTime = System.currentTimeMillis();
        LOGGER.info("REST request to list sensitive words with page={}, size={}, sort={}", pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        Page<SensitiveWordResponse> response = sensitiveWordService.findAll(pageable);

        LOGGER.info(
                "Sensitive-word list request completed with returnedElements={}, totalElements={}. Duration: {} ms",
                response.getNumberOfElements(),
                response.getTotalElements(),
                System.currentTimeMillis() - startTime
        );

        return withTraceHeader(ResponseEntity.ok()).body(response);
    }

    @GetMapping("/sensitive-words/{id}")
    @Operation(summary = "Get a sensitive word by id", description = "Returns one sensitive-word record by database identifier.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Sensitive word found",
                    headers = @Header(name = TRACE_ID_HEADER, description = "Trace id for log correlation."),
                    content = @Content(schema = @Schema(implementation = SensitiveWordResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid identifier",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Sensitive word not found",
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
            )
    })
    public ResponseEntity<SensitiveWordResponse> findById(
            @Parameter(description = "Sensitive-word identifier.", example = "1")
            @Positive(message = "id must be greater than zero")
            @PathVariable Long id
    ) {
        long startTime = System.currentTimeMillis();
        LOGGER.info("REST request to get sensitive word with id={}", id);

        SensitiveWordResponse response = sensitiveWordService.findById(id);

        LOGGER.info(
                "Sensitive-word lookup completed for id={}. Duration: {} ms",
                id,
                System.currentTimeMillis() - startTime
        );

        return withTraceHeader(ResponseEntity.ok()).body(response);
    }

    @PostMapping("/sensitive-words")
    @Operation(
            summary = "Create a sensitive word",
            description = "Creates a new sensitive word or phrase. Words must be unique after trimming and lowercasing."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Sensitive word created",
                    headers = {
                            @Header(name = "Location", description = "URL of the created sensitive word."),
                            @Header(name = TRACE_ID_HEADER, description = "Trace id for log correlation.")
                    },
                    content = @Content(schema = @Schema(implementation = SensitiveWordResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request payload",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Duplicate word",
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
            )
    })
    public ResponseEntity<SensitiveWordResponse> create(@Valid @RequestBody CreateSensitiveWordRequest request) {
        long startTime = System.currentTimeMillis();
        // Log metadata only; the actual word can be sensitive.
        LOGGER.info(
                "REST request to create sensitive word with wordLength={}",
                request.word() == null ? 0 : request.word().length()
        );

        SensitiveWordResponse created = sensitiveWordService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();

        LOGGER.info(
                "Sensitive-word create completed for id={}. Duration: {} ms",
                created.id(),
                System.currentTimeMillis() - startTime
        );

        return withTraceHeader(ResponseEntity.created(location)).body(created);
    }

    @PatchMapping("/sensitive-words/{id}")
    @Operation(
            summary = "Partially update a sensitive word",
            description = "Applies a partial update to an existing sensitive word. Omitted fields keep their current values."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Sensitive word updated",
                    headers = @Header(name = TRACE_ID_HEADER, description = "Trace id for log correlation."),
                    content = @Content(schema = @Schema(implementation = SensitiveWordResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid identifier or payload",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Sensitive word not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Duplicate word",
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
            )
    })
    public ResponseEntity<SensitiveWordResponse> update(
            @Parameter(description = "Sensitive-word identifier.", example = "1")
            @Positive(message = "id must be greater than zero")
            @PathVariable Long id,
            @Valid @org.springframework.web.bind.annotation.RequestBody UpdateSensitiveWordRequest request
    ) {
        long startTime = System.currentTimeMillis();
        LOGGER.info(
                "REST request to update sensitive word with id={}, wordProvided={}",
                id,
                request.word() != null
        );

        SensitiveWordResponse response = sensitiveWordService.update(id, request);

        LOGGER.info(
                "Sensitive-word update completed for id={}. Duration: {} ms",
                response.id(),
                System.currentTimeMillis() - startTime
        );

        return withTraceHeader(ResponseEntity.ok()).body(response);
    }

    @DeleteMapping("/sensitive-words/{id}")
    @Operation(
            summary = "Delete a sensitive word",
            description = "Permanently removes the sensitive-word record from the database."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Sensitive word deleted",
                    headers = @Header(name = TRACE_ID_HEADER, description = "Trace id for log correlation."),
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid identifier",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Sensitive word not found",
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
            )
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Sensitive-word identifier.", example = "1")
            @Positive(message = "id must be greater than zero")
            @PathVariable Long id
    ) {
        long startTime = System.currentTimeMillis();
        LOGGER.info("REST request to delete sensitive word with id={}", id);

        sensitiveWordService.delete(id);

        LOGGER.info(
                "Sensitive-word delete completed for id={}. Duration: {} ms",
                id,
                System.currentTimeMillis() - startTime
        );

        return withTraceHeader(ResponseEntity.status(HttpStatus.NO_CONTENT)).build();
    }

    private ResponseEntity.BodyBuilder withTraceHeader(ResponseEntity.BodyBuilder builder) {
        // Brave creates the span; API responses expose its trace id for downstream support correlation.
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
