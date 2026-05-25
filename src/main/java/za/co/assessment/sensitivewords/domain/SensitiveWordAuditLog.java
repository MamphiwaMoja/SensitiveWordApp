package za.co.assessment.sensitivewords.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import za.co.assessment.sensitivewords.config.Constants;

@Entity
@Table(name = "sensitive_word_audit_log", schema = Constants.DB_SCHEMA)
public class SensitiveWordAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensitive_word_id")
    private SensitiveWord sensitiveWord;

    @Column(name = "action_type", nullable = false, length = 20)
    private String actionType;

    @Lob
    @Column(name = "old_value", columnDefinition = "NVARCHAR(MAX)")
    private String oldValue;

    @Lob
    @Column(name = "new_value", columnDefinition = "NVARCHAR(MAX)")
    private String newValue;

    @Column(name = "changed_by", nullable = false, length = 200)
    private String changedBy = Constants.SYSTEM_ACCOUNT;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @PrePersist
    void onCreate() {
        // Audit rows are append-only snapshots, so creation metadata is set once on insert.
        if (changedAt == null) {
            changedAt = LocalDateTime.now(ZoneOffset.UTC);
        }
        if (changedBy == null || changedBy.isBlank()) {
            changedBy = Constants.SYSTEM_ACCOUNT;
        }
    }

    public Long getId() {
        return id;
    }

    public SensitiveWord getSensitiveWord() {
        return sensitiveWord;
    }

    public void setSensitiveWord(SensitiveWord sensitiveWord) {
        this.sensitiveWord = sensitiveWord;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }
}
