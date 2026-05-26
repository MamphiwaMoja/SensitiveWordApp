package za.co.assessment.sensitivewords.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(name = "SensitiveWordResponse", description = "Sensitive word returned by the CRUD API.")
public record SensitiveWordResponse(
        @Schema(description = "Database identifier of the sensitive word.", example = "10")
        Long id,
        @Schema(description = "Original word or phrase configured for matching.", example = "SELECT")
        String word,
        @Schema(description = "Lowercase trimmed value used for duplicate checks.", example = "select")
        String normalizedWord,
        @Schema(description = "Severity ranking stored with the sensitive word.", minimum = "1", maximum = "255", example = "1")
        Integer severityLevel,
        @Schema(description = "Whether the word participates in sanitization.", example = "true")
        Boolean active,
        @Schema(description = "UTC timestamp from which the word is effective.", example = "2026-05-26T09:14:33.787")
        LocalDateTime effectiveFrom,
        @Schema(description = "UTC timestamp when the record was created.", example = "2026-05-26T09:14:33.787")
        LocalDateTime createdAt,
        @Schema(description = "UTC timestamp when the record was last updated.", nullable = true, example = "2026-05-26T09:20:13.439")
        LocalDateTime updatedAt
) {
}
