package za.co.assessment.sensitivewords.service.audit;

import org.springframework.stereotype.Service;
import za.co.assessment.sensitivewords.config.Constants;
import za.co.assessment.sensitivewords.domain.SensitiveWord;
import za.co.assessment.sensitivewords.domain.SensitiveWordAuditLog;
import za.co.assessment.sensitivewords.repository.SensitiveWordAuditLogRepository;

@Service
public class DatabaseSensitiveWordAuditService implements SensitiveWordAuditService {

    private final SensitiveWordAuditLogRepository auditLogRepository;

    public DatabaseSensitiveWordAuditService(SensitiveWordAuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public void recordInsert(SensitiveWord word) {
        audit(word, "INSERT", null, snapshot(word));
    }

    @Override
    public void recordUpdate(SensitiveWord word, String oldSnapshot) {
        audit(word, "UPDATE", oldSnapshot, snapshot(word));
    }

    @Override
    public void recordDeactivate(SensitiveWord word, String oldSnapshot) {
        audit(word, "DEACTIVATE", oldSnapshot, snapshot(word));
    }

    @Override
    public String snapshot(SensitiveWord word) {
        return "{" +
                "\"id\":" + word.getId() +
                ",\"word\":\"" + escape(word.getWord()) + "\"" +
                ",\"matchType\":\"" + word.getMatchType() + "\"" +
                ",\"replacementValue\":\"" + escape(word.getReplacementValue()) + "\"" +
                ",\"severityLevel\":" + word.getSeverityLevel() +
                ",\"active\":" + word.getActive() +
                "}";
    }

    private void audit(SensitiveWord word, String action, String oldValue, String newValue) {
        SensitiveWordAuditLog log = new SensitiveWordAuditLog();
        log.setSensitiveWord(word);
        log.setActionType(action);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setChangedBy(Constants.SYSTEM_ACCOUNT);
        auditLogRepository.save(log);
    }

    private String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
