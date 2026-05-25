package za.co.assessment.sensitivewords.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import za.co.assessment.sensitivewords.domain.MatchType;

@Schema(name = "UpdateSensitiveWordRequest", description = "Payload used to partially update a sensitive-word rule.")
public record UpdateSensitiveWordRequest(
        @Schema(description = "Optional replacement category identifier.", example = "2")
        Long categoryId,

        @Schema(description = "New word or pattern to match.", example = "restricted phrase")
        @Size(max = 255, message = "word must not exceed 255 characters")
        String word,

        @Schema(description = "New replacement value.", example = "[redacted]")
        @Size(max = 255, message = "replacementValue must not exceed 255 characters")
        String replacementValue,

        @Schema(description = "New match strategy.", example = "EXACT")
        MatchType matchType,

        @Schema(description = "New severity ranking.", example = "3")
        @Min(value = 1, message = "severityLevel must be between 1 and 5")
        @Max(value = 5, message = "severityLevel must be between 1 and 5")
        Integer severityLevel,

        @Schema(description = "Updated case-sensitivity flag.", example = "false")
        Boolean caseSensitive,
        @Schema(description = "Updated activation flag.", example = "true")
        Boolean active,

        @Schema(description = "Updated operator notes.", example = "Tightened to exact-match only.")
        @Size(max = 500, message = "notes must not exceed 500 characters")
        String notes
) {
}
