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
    public void recordDelete(SensitiveWord word, String oldSnapshot) {
        audit(null, "DELETE", oldSnapshot, null);
    }

    @Override
    public String snapshot(SensitiveWord word) {
        // Store a compact JSON-like snapshot so audit reads do not depend on lazy entity state later.
        return "{" +
                "\"id\":" + word.getId() +
                ",\"word\":\"" + escape(word.getWord()) + "\"" +
                ",\"severityLevel\":" + word.getSeverityLevel() +
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
        // Snapshot strings are assembled without a JSON mapper to keep the audit dependency surface small.
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
