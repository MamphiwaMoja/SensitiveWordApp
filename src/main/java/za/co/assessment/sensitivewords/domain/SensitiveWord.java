package za.co.assessment.sensitivewords.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import za.co.assessment.sensitivewords.config.Constants;

@Entity
@Table(name = "sensitive_words", schema = Constants.DB_SCHEMA)
public class SensitiveWord extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sensitive_word_id")
    private Long id;

    @Column(name = "word", nullable = false, length = 510)
    private String word;

    // Computed by SQL Server to enforce consistent duplicate checks independent of input case.
    @Column(name = "normalized_word", insertable = false, updatable = false, length = 510)
    private String normalizedWord;

    @Column(name = "severity_level", nullable = false)
    private Integer severityLevel = 1;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @Column(name = "effective_from", nullable = false)
    private LocalDateTime effectiveFrom;

    @PrePersist
    void onCreate() {
        onCreateAudit();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        // Service code applies the same defaults; this guards direct persistence paths as well.
        if (effectiveFrom == null) {
            effectiveFrom = now;
        }
        if (getCreatedBy() == null || getCreatedBy().isBlank()) {
            setCreatedBy(Constants.SYSTEM_ACCOUNT);
        }
        if (severityLevel == null) {
            severityLevel = 1;
        }
        if (active == null) {
            active = true;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getNormalizedWord() {
        return normalizedWord;
    }

    public void setNormalizedWord(String normalizedWord) {
        this.normalizedWord = normalizedWord;
    }

    public Integer getSeverityLevel() {
        return severityLevel;
    }

    public void setSeverityLevel(Integer severityLevel) {
        this.severityLevel = severityLevel;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDateTime effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }
}
