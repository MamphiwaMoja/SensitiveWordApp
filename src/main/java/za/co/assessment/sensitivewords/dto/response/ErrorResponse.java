package za.co.assessment.sensitivewords.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import za.co.assessment.sensitivewords.web.rest.errors.ErrorMessages;

@Schema(name = "ErrorResponse", description = "Standard API error response.")
public record ErrorResponse(
        @Schema(example = "2026-05-23T15:10:00Z")
        Instant timestamp,
        @Schema(example = "400")
        int status,
        @Schema(example = "Bad Request")
        String error,
        @Schema(example = ErrorMessages.VALIDATION_FAILED)
        String message,
        @Schema(example = "/api/v1/sanitize")
        String path,
        @Schema(description = "Optional structured error details.")
        Object details
) {
}
