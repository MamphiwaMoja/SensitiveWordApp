package za.co.assessment.sensitivewords.mapper;

import org.springframework.stereotype.Component;
import za.co.assessment.sensitivewords.domain.SensitiveWord;
import za.co.assessment.sensitivewords.domain.SensitiveWordCategory;
import za.co.assessment.sensitivewords.dto.response.SensitiveWordResponse;

@Component
public class SensitiveWordMapper {

    public SensitiveWordResponse toResponse(SensitiveWord word) {
        SensitiveWordCategory category = word.getCategory();

        return new SensitiveWordResponse(
                word.getId(),
                category == null ? null : category.getId(),
                category == null ? null : category.getCode(),
                category == null ? null : category.getName(),
                word.getWord(),
                word.getNormalizedWord(),
                word.getReplacementValue(),
                word.getMatchType(),
                word.getSeverityLevel(),
                word.getCaseSensitive(),
                word.getActive(),
                word.getEffectiveFrom(),
                word.getEffectiveTo(),
                word.getNotes(),
                word.getCreatedAt(),
                word.getUpdatedAt()
        );
    }
}
