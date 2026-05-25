package za.co.assessment.sensitivewords.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import za.co.assessment.sensitivewords.config.Constants;
import za.co.assessment.sensitivewords.domain.MatchType;

@Schema(name = "SensitiveWordResponse", description = "Sensitive-word rule returned by the CRUD API.")
public record SensitiveWordResponse(
        @Schema(example = "10")
        Long id,
        @Schema(example = "1")
        Long categoryId,
        @Schema(example = "PROFANITY")
        String categoryCode,
        @Schema(example = "Profanity")
        String categoryName,
        @Schema(example = "testbadword")
        String word,
        @Schema(example = "testbadword")
        String normalizedWord,
        @Schema(example = Constants.DEFAULT_REPLACEMENT)
        String replacementValue,
        @Schema(example = "CONTAINS")
        MatchType matchType,
        @Schema(example = "2")
        Integer severityLevel,
        @Schema(example = "false")
        Boolean caseSensitive,
        @Schema(example = "true")
        Boolean active,
        @Schema(example = "2026-05-23T13:00:00")
        LocalDateTime effectiveFrom,
        @Schema(nullable = true, example = "2026-05-24T13:00:00")
        LocalDateTime effectiveTo,
        @Schema(example = "Created during assessment setup.")
        String notes,
        @Schema(example = "2026-05-23T13:00:00")
        LocalDateTime createdAt,
        @Schema(nullable = true, example = "2026-05-23T13:15:00")
        LocalDateTime updatedAt
) {
}
