package za.co.assessment.sensitivewords.service.audit;

import za.co.assessment.sensitivewords.domain.SensitiveWord;

public interface SensitiveWordAuditService {

    void recordInsert(SensitiveWord word);

    void recordUpdate(SensitiveWord word, String oldSnapshot);

    void recordDelete(SensitiveWord word, String oldSnapshot);

    String snapshot(SensitiveWord word);
}
