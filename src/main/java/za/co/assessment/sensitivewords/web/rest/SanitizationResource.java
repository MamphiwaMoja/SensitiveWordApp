package za.co.assessment.sensitivewords.web.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@RequestMapping(ApiPaths.SANITIZE)
@Tag(name = "Sanitization", description = "APIs for sanitizing incoming text")
public class SanitizationResource {

    private final SanitizationService sanitizationService;

    public SanitizationResource(SanitizationService sanitizationService) {
        this.sanitizationService = sanitizationService;
    }

    @PostMapping
    @Operation(
            summary = "Sanitize input text using active sensitive-word rules",
            description = "Applies active sensitive-word rules in priority order and returns the sanitized text. "
                    + "Request payload persistence is disabled by default and must be explicitly enabled."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Text sanitized successfully",
                    content = @Content(schema = @Schema(implementation = SanitizeTextResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request payload",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = ErrorMessages.UNEXPECTED_SERVER_ERROR,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<SanitizeTextResponse> sanitize(@Valid @RequestBody SanitizeTextRequest request) {
        return ResponseEntity.ok(sanitizationService.sanitize(request));
    }
}
