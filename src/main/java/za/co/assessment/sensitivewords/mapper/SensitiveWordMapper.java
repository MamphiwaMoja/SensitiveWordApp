package za.co.assessment.sensitivewords.mapper;

import org.springframework.stereotype.Component;
import za.co.assessment.sensitivewords.domain.SensitiveWord;
import za.co.assessment.sensitivewords.dto.response.SensitiveWordResponse;

@Component
public class SensitiveWordMapper {

    public SensitiveWordResponse toResponse(SensitiveWord word) {
        return new SensitiveWordResponse(
                word.getId(),
                word.getWord(),
                word.getNormalizedWord(),
                word.getSeverityLevel(),
                word.getEffectiveFrom(),
                word.getCreatedAt(),
                word.getUpdatedAt()
        );
    }
}
