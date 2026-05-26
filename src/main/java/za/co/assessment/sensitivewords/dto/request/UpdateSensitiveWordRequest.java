package za.co.assessment.sensitivewords.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@Schema(name = "UpdateSensitiveWordRequest", description = "Payload used to partially update a sensitive word.")
public record UpdateSensitiveWordRequest(
        @Schema(description = "Replacement exact word or phrase to mask. Leave omitted to keep the existing value.", example = "SELECT * FROM")
        @Size(max = 510, message = "word must not exceed 510 characters")
        String word,

        @Schema(description = "New severity ranking. Leave omitted to keep the existing value.", minimum = "1", maximum = "255", example = "2")
        @Min(value = 1, message = "severityLevel must be between 1 and 255")
        @Max(value = 255, message = "severityLevel must be between 1 and 255")
        Integer severityLevel,

        @Schema(description = "Updated activation flag. Leave omitted to keep the existing value.", example = "true")
        Boolean active
) {
}
