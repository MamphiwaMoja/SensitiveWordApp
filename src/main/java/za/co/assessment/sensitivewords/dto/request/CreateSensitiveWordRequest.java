package za.co.assessment.sensitivewords.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "CreateSensitiveWordRequest", description = "Payload used to create a sensitive word.")
public record CreateSensitiveWordRequest(
        @Schema(description = "Optional category identifier.", example = "1")
        Long categoryId,

        @Schema(description = "Word to mask when it appears in input text.", example = "testbadword")
        @NotBlank(message = "word is required")
        @Size(max = 510, message = "word must not exceed 510 characters")
        String word,

        @Schema(description = "Severity ranking stored with the sensitive word.", example = "2")
        @Min(value = 1, message = "severityLevel must be between 1 and 255")
        @Max(value = 255, message = "severityLevel must be between 1 and 255")
        Integer severityLevel,

        @Schema(description = "When false, the word is created in an inactive state.", example = "true")
        Boolean active
) {
}
