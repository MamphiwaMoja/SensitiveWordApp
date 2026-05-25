package za.co.assessment.sensitivewords.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@Schema(name = "SanitizeTextResponse", description = "Response returned after sanitizing input text.")
public record SanitizeTextResponse(
        @Schema(description = "Request identifier when request persistence is enabled.", example = "6d64c6b8-8f97-437f-90a0-3cc7c719f3d1")
        UUID requestId,
        @Schema(example = "This message contains testbadword.")
        String originalText,
        @Schema(example = "This message contains ***.")
        String sanitizedText,
        @Schema(example = "1")
        Integer matchedWordsCount,
        @Schema(example = "3")
        Integer processingTimeMs,
        List<MatchedWordResponse> matchedWords
) {
}
