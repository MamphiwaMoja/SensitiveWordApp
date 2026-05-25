package za.co.assessment.sensitivewords.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(name = "SensitiveWordResponse", description = "Sensitive word returned by the CRUD API.")
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
        @Schema(example = "2")
        Integer severityLevel,
        @Schema(example = "true")
        Boolean active,
        @Schema(example = "2026-05-23T13:00:00")
        LocalDateTime effectiveFrom,
        @Schema(example = "2026-05-23T13:00:00")
        LocalDateTime createdAt,
        @Schema(nullable = true, example = "2026-05-23T13:15:00")
        LocalDateTime updatedAt
) {
}
