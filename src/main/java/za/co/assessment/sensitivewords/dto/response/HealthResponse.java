package za.co.assessment.sensitivewords.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "HealthResponse", description = "Application health-check response.")
public record HealthResponse(
        @Schema(description = "Current application health status.", example = "UP")
        String status,
        @Schema(description = "Service name.", example = "sensitive-words-service")
        String service,
        @Schema(description = "UTC timestamp when the health check was evaluated.", example = "2026-05-26T10:00:00Z")
        String checkedAt
) {
}
