package za.co.assessment.sensitivewords.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "MatchedWordResponse", description = "Details about a sensitive word that matched during sanitization.")
public record MatchedWordResponse(
        @Schema(description = "Identifier of the sensitive word that matched.", example = "42")
        Long sensitiveWordId,
        @Schema(description = "Configured sensitive word or phrase that matched.", example = "SELECT")
        String word,
        @Schema(description = "Severity ranking stored with the matched word.", example = "1")
        Integer severityLevel,
        @Schema(description = "Number of replacements made for this word.", example = "1")
        Integer replacementCount
) {
}
