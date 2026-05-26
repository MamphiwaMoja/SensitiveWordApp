package za.co.assessment.sensitivewords.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import za.co.assessment.sensitivewords.web.rest.errors.ErrorMessages;

@Schema(name = "ErrorResponse", description = "Standard API error response.")
public record ErrorResponse(
        @Schema(description = "UTC timestamp when the error response was created.", example = "2026-05-26T09:20:49.456073050Z")
        Instant timestamp,
        @Schema(description = "HTTP status code.", example = "400")
        int status,
        @Schema(description = "HTTP status reason phrase.", example = "Bad Request")
        String error,
        @Schema(description = "Client-facing error message.", example = ErrorMessages.VALIDATION_FAILED)
        String message,
        @Schema(description = "Request path that failed.", example = "/api/v1/sanitize")
        String path,
        @Schema(description = "Optional structured error details such as field validation failures.", nullable = true)
        Object details
) {
}
