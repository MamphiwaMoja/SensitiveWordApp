package za.co.assessment.sensitivewords.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;

import java.util.List;
import java.util.UUID;

@Schema(name = "SanitizeTextResponse", description = "Response returned after sanitizing input text.")
public record SanitizeTextResponse(
        @Schema(description = "Request identifier when request persistence is enabled; null when persistence is disabled.", nullable = true, example = "6d64c6b8-8f97-437f-90a0-3cc7c719f3d1")
        UUID requestId,
        @Schema(description = "Original text received by the API.", example = "hello On SELECT from accounts")
        String originalText,
        @Schema(description = "Sanitized text after replacing exact sensitive-word matches.", example = "hello *** *** from accounts")
        String sanitizedText,
        @Schema(description = "Total number of replacements made across all matched words.", example = "2")
        Integer matchedWordsCount,
        @Schema(description = "Processing time in milliseconds.", example = "3")
        Integer processingTimeMs,
        @ArraySchema(schema = @Schema(implementation = MatchedWordResponse.class), arraySchema = @Schema(description = "Matched words and replacement counts."))
        List<MatchedWordResponse> matchedWords
) {
}
