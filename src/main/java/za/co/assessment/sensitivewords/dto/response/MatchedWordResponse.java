package za.co.assessment.sensitivewords.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "MatchedWordResponse", description = "Details about a sensitive word that matched during sanitization.")
public record MatchedWordResponse(
        @Schema(example = "3")
        Long sensitiveWordId,
        @Schema(example = "scam")
        String word,
        @Schema(example = "4")
        Integer severityLevel,
        @Schema(example = "2")
        Integer replacementCount
) {
}
