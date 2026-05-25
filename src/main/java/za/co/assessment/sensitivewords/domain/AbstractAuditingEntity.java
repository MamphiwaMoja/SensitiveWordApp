package za.co.assessment.sensitivewords.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import za.co.assessment.sensitivewords.config.Constants;

@MappedSuperclass
public abstract class AbstractAuditingEntity {

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", nullable = false, length = 200)
    private String createdBy = Constants.SYSTEM_ACCOUNT;

    @Column(name = "updated_by", length = 200)
    private String updatedBy;

    @PrePersist
    protected void onCreateAudit() {
        // Database scripts own the schema, so entity callbacks fill only application-level audit defaults.
        if (createdAt == null) {
            createdAt = LocalDateTime.now(ZoneOffset.UTC);
        }
        if (createdBy == null || createdBy.isBlank()) {
            createdBy = Constants.SYSTEM_ACCOUNT;
        }
    }

    @PreUpdate
    protected void onUpdateAudit() {
        // Always stamp updates in UTC to keep audit ordering stable across deployment regions.
        updatedAt = LocalDateTime.now(ZoneOffset.UTC);
        if (updatedBy == null || updatedBy.isBlank()) {
            updatedBy = Constants.SYSTEM_ACCOUNT;
        }
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
