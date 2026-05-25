package za.co.assessment.sensitivewords.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import za.co.assessment.sensitivewords.config.Constants;

@Entity
@Table(name = "sensitive_word_categories", schema = Constants.DB_SCHEMA)
public class SensitiveWordCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    @Column(name = "category_code", nullable = false, length = 50)
    private String code;

    @Column(name = "category_name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", nullable = false, length = 200)
    private String createdBy = Constants.SYSTEM_ACCOUNT;

    @Column(name = "updated_by", length = 200)
    private String updatedBy;

    @PrePersist
    void onCreate() {
        // Categories are managed internally; default missing audit fields to the system actor.
        if (createdAt == null) {
            createdAt = LocalDateTime.now(ZoneOffset.UTC);
        }
        if (createdBy == null || createdBy.isBlank()) {
            createdBy = Constants.SYSTEM_ACCOUNT;
        }
        if (active == null) {
            active = true;
        }
    }

    @PreUpdate
    void onUpdate() {
        // Keep category updates aligned with the same UTC audit model as sensitive-word changes.
        updatedAt = LocalDateTime.now(ZoneOffset.UTC);
        if (updatedBy == null || updatedBy.isBlank()) {
            updatedBy = Constants.SYSTEM_ACCOUNT;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
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
