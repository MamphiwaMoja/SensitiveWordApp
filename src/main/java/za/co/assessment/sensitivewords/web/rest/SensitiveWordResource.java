package za.co.assessment.sensitivewords.web.rest;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springdoc.core.annotations.ParameterObject;
import za.co.assessment.sensitivewords.dto.request.CreateSensitiveWordRequest;
import za.co.assessment.sensitivewords.dto.request.UpdateSensitiveWordRequest;
import za.co.assessment.sensitivewords.dto.response.ErrorResponse;
import za.co.assessment.sensitivewords.dto.response.SensitiveWordResponse;
import za.co.assessment.sensitivewords.service.SensitiveWordService;

import java.net.URI;

@RestController
@RequestMapping(ApiPaths.API_V1)
@Validated
@Tag(name = "Sensitive Words", description = "CRUD APIs for sensitive-word rules")
public class SensitiveWordResource {

    private final SensitiveWordService sensitiveWordService;

    public SensitiveWordResource(SensitiveWordService sensitiveWordService) {
        this.sensitiveWordService = sensitiveWordService;
    }

    @GetMapping("/sensitive-words")
    @Operation(
            summary = "List sensitive-word rules",
            description = "Returns a paginated list of sensitive-word rules, including active and inactive records."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Sensitive-word rules returned successfully",
            content = @Content(schema = @Schema(implementation = SensitiveWordResponse.class))
    )
    public ResponseEntity<Page<SensitiveWordResponse>> findAll(
            @ParameterObject
            @PageableDefault(size = 20, sort = "id") Pageable pageable
    ) {
        return ResponseEntity.ok(sensitiveWordService.findAll(pageable));
    }

    @GetMapping("/sensitive-words/{id}")
    @Operation(summary = "Get a sensitive-word rule by id")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Sensitive-word rule found",
                    content = @Content(schema = @Schema(implementation = SensitiveWordResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid identifier",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Sensitive-word rule not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<SensitiveWordResponse> findById(
            @Parameter(description = "Sensitive-word identifier.", example = "1")
            @Positive(message = "id must be greater than zero")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(sensitiveWordService.findById(id));
    }

    @PostMapping("/sensitive-words")
    @Operation(
            summary = "Create a sensitive-word rule",
            description = "Creates a new sensitive-word rule. REGEX rules are validated before being persisted."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Sensitive-word rule created",
                    content = @Content(schema = @Schema(implementation = SensitiveWordResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request payload",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Duplicate active rule",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<SensitiveWordResponse> create(@Valid @RequestBody CreateSensitiveWordRequest request) {
        SensitiveWordResponse created = sensitiveWordService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PatchMapping("/sensitive-words/{id}")
    @Operation(
            summary = "Partially update a sensitive-word rule",
            description = "Applies a partial update to an existing sensitive-word rule."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Sensitive-word rule updated",
                    content = @Content(schema = @Schema(implementation = SensitiveWordResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid identifier or payload",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Sensitive-word rule not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Duplicate active rule",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<SensitiveWordResponse> update(
            @Parameter(description = "Sensitive-word identifier.", example = "1")
            @Positive(message = "id must be greater than zero")
            @PathVariable Long id,
            @Valid @org.springframework.web.bind.annotation.RequestBody UpdateSensitiveWordRequest request
    ) {
        return ResponseEntity.ok(sensitiveWordService.update(id, request));
    }

    @DeleteMapping("/sensitive-words/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deactivate a sensitive-word rule")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Sensitive-word rule deactivated"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid identifier",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Sensitive-word rule not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public void deactivate(
            @Parameter(description = "Sensitive-word identifier.", example = "1")
            @Positive(message = "id must be greater than zero")
            @PathVariable Long id
    ) {
        sensitiveWordService.deactivate(id);
    }

}
