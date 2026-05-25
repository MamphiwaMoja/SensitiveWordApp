package za.co.assessment.sensitivewords.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import za.co.assessment.sensitivewords.config.Constants;

@Entity
@Table(name = "sanitization_requests", schema = Constants.DB_SCHEMA)
public class SanitizationRequestLog {

    @Id
    @Column(name = "request_id", nullable = false)
    private UUID requestId;

    @Column(name = "source_system", length = 100)
    private String sourceSystem;

    @Lob
    @Column(name = "original_text", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String originalText;

    @Lob
    @Column(name = "sanitized_text", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String sanitizedText;

    @Column(name = "matched_words_count", nullable = false)
    private Integer matchedWordsCount = 0;

    @Column(name = "processing_time_ms")
    private Integer processingTimeMs;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        // Request logging is opt-in, but persisted rows still need a complete audit envelope.
        if (requestId == null) {
            requestId = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now(ZoneOffset.UTC);
        }
        if (matchedWordsCount == null) {
            matchedWordsCount = 0;
        }
    }

    public UUID getRequestId() {
        return requestId;
    }

    public void setRequestId(UUID requestId) {
        this.requestId = requestId;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public String getSanitizedText() {
        return sanitizedText;
    }

    public void setSanitizedText(String sanitizedText) {
        this.sanitizedText = sanitizedText;
    }

    public Integer getMatchedWordsCount() {
        return matchedWordsCount;
    }

    public void setMatchedWordsCount(Integer matchedWordsCount) {
        this.matchedWordsCount = matchedWordsCount;
    }

    public Integer getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(Integer processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
