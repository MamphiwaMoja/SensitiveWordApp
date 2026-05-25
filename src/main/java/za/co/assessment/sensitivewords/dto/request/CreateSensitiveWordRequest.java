package za.co.assessment.sensitivewords.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import za.co.assessment.sensitivewords.config.Constants;
import za.co.assessment.sensitivewords.domain.MatchType;

@Schema(name = "CreateSensitiveWordRequest", description = "Payload used to create a sensitive-word rule.")
public record CreateSensitiveWordRequest(
        @Schema(description = "Optional category identifier.", example = "1")
        Long categoryId,

        @Schema(description = "Word or pattern to match.", example = "testbadword")
        @NotBlank(message = "word is required")
        @Size(max = 255, message = "word must not exceed 255 characters")
        String word,

        @Schema(description = "Replacement text that should be inserted into the sanitized output.", example = Constants.DEFAULT_REPLACEMENT)
        @Size(max = 255, message = "replacementValue must not exceed 255 characters")
        String replacementValue,

        @Schema(description = "Match strategy for the rule.", example = "CONTAINS")
        MatchType matchType,

        @Schema(description = "Severity ranking used to order rules from highest to lowest.", example = "2")
        @Min(value = 1, message = "severityLevel must be between 1 and 5")
        @Max(value = 5, message = "severityLevel must be between 1 and 5")
        Integer severityLevel,

        @Schema(description = "When true, matching becomes case-sensitive.", example = "false")
        Boolean caseSensitive,
        @Schema(description = "When false, the rule is created in an inactive state.", example = "true")
        Boolean active,

        @Schema(description = "Optional notes for the rule.", example = "Blocks a known profanity term.")
        @Size(max = 500, message = "notes must not exceed 500 characters")
        String notes
) {
}
