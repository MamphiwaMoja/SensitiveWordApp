package za.co.assessment.sensitivewords.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private SensitiveWordCategory category;

    @Column(name = "word", nullable = false, length = 255)
    private String word;

    @Column(name = "normalized_word", insertable = false, updatable = false, length = 255)
    private String normalizedWord;

    @Column(name = "replacement_value", nullable = false, length = 255)
    private String replacementValue = Constants.DEFAULT_REPLACEMENT;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_type", nullable = false, length = 20)
    private MatchType matchType = MatchType.CONTAINS;

    @Column(name = "severity_level", nullable = false)
    private Integer severityLevel = 1;

    @Column(name = "is_case_sensitive", nullable = false)
    private Boolean caseSensitive = false;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @Column(name = "effective_from", nullable = false)
    private LocalDateTime effectiveFrom;

    @Column(name = "effective_to")
    private LocalDateTime effectiveTo;

    @Column(name = "notes", length = 500)
    private String notes;

    @PrePersist
    void onCreate() {
        onCreateAudit();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (effectiveFrom == null) {
            effectiveFrom = now;
        }
        if (getCreatedBy() == null || getCreatedBy().isBlank()) {
            setCreatedBy(Constants.SYSTEM_ACCOUNT);
        }
        if (replacementValue == null || replacementValue.isBlank()) {
            replacementValue = Constants.DEFAULT_REPLACEMENT;
        }
        if (matchType == null) {
            matchType = MatchType.CONTAINS;
        }
        if (severityLevel == null) {
            severityLevel = 1;
        }
        if (caseSensitive == null) {
            caseSensitive = false;
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

    public SensitiveWordCategory getCategory() {
        return category;
    }

    public void setCategory(SensitiveWordCategory category) {
        this.category = category;
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

    public String getReplacementValue() {
        return replacementValue;
    }

    public void setReplacementValue(String replacementValue) {
        this.replacementValue = replacementValue;
    }

    public MatchType getMatchType() {
        return matchType;
    }

    public void setMatchType(MatchType matchType) {
        this.matchType = matchType;
    }

    public Integer getSeverityLevel() {
        return severityLevel;
    }

    public void setSeverityLevel(Integer severityLevel) {
        this.severityLevel = severityLevel;
    }

    public Boolean getCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(Boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
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

    public LocalDateTime getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(LocalDateTime effectiveTo) {
        this.effectiveTo = effectiveTo;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
