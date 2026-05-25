package za.co.assessment.sensitivewords.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import za.co.assessment.sensitivewords.domain.MatchType;

@Schema(name = "MatchedWordResponse", description = "Details about a rule that matched during sanitization.")
public record MatchedWordResponse(
        @Schema(example = "3")
        Long sensitiveWordId,
        @Schema(example = "scam")
        String word,
        @Schema(example = "CONTAINS")
        MatchType matchType,
        @Schema(example = "4")
        Integer severityLevel,
        @Schema(example = "2")
        Integer replacementCount
) {
}
