package za.co.assessment.sensitivewords.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import za.co.assessment.sensitivewords.dto.request.CreateSensitiveWordRequest;
import za.co.assessment.sensitivewords.dto.request.UpdateSensitiveWordRequest;
import za.co.assessment.sensitivewords.dto.response.SensitiveWordResponse;

public interface SensitiveWordService {

    Page<SensitiveWordResponse> findAll(Pageable pageable);

    SensitiveWordResponse findById(Long id);

    SensitiveWordResponse create(CreateSensitiveWordRequest request);

    SensitiveWordResponse update(Long id, UpdateSensitiveWordRequest request);

    void delete(Long id);
}
