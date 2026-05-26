package za.co.assessment.sensitivewords.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "CreateSensitiveWordRequest", description = "Payload used to create a sensitive word or phrase.")
public record CreateSensitiveWordRequest(
        @Schema(description = "Exact word or phrase to mask when it appears in input text.", example = "SELECT")
        @NotBlank(message = "word is required")
        @Size(max = 510, message = "word must not exceed 510 characters")
        String word,

        @Schema(description = "Severity ranking stored with the sensitive word. Defaults to 1 when omitted.", minimum = "1", maximum = "255", example = "1")
        @Min(value = 1, message = "severityLevel must be between 1 and 255")
        @Max(value = 255, message = "severityLevel must be between 1 and 255")
        Integer severityLevel
) {
}
