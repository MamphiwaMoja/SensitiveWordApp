package za.co.assessment.sensitivewords.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "SanitizeTextRequest", description = "Payload containing the text to sanitize.")
public record SanitizeTextRequest(
        @Schema(
                description = "Input text that should be sanitized using active sensitive-word rules.",
                example = "This message contains testbadword and a restricted phrase."
        )
        @NotBlank(message = "inputText is required")
        @Size(max = 5000, message = "inputText must not exceed 5000 characters")
        String inputText,

        @Schema(
                description = "Optional source system identifier for tracing.",
                example = "crm-service"
        )
        @Size(max = 100, message = "sourceSystem must not exceed 100 characters")
        String sourceSystem,

        @Schema(
                description = "When true, persist the request and response payloads for audit/troubleshooting.",
                example = "false",
                defaultValue = "false"
        )
        Boolean persistRequest
) {
    public boolean shouldPersistRequest() {
        return Boolean.TRUE.equals(persistRequest);
    }
}
