package za.co.assessment.sensitivewords.service;

import za.co.assessment.sensitivewords.dto.request.SanitizeTextRequest;
import za.co.assessment.sensitivewords.dto.response.SanitizeTextResponse;

public interface SanitizationService {

    SanitizeTextResponse sanitize(SanitizeTextRequest request);
}
