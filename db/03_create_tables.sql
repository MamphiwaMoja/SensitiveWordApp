SET ANSI_NULLS ON;
GO

SET QUOTED_IDENTIFIER ON;
GO

USE SensitiveWordsDb;
GO

IF OBJECT_ID(N'sw.sensitive_word_audit_log', N'U') IS NOT NULL
BEGIN
    DROP TABLE sw.sensitive_word_audit_log;
END;
GO

IF OBJECT_ID(N'sw.sanitization_requests', N'U') IS NOT NULL
BEGIN
    DROP TABLE sw.sanitization_requests;
END;
GO

IF OBJECT_ID(N'sw.sensitive_words', N'U') IS NOT NULL
BEGIN
    DROP TABLE sw.sensitive_words;
END;
GO

CREATE TABLE sw.sensitive_words (
    sensitive_word_id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    word NVARCHAR(510) NOT NULL,
    normalized_word AS LOWER(LTRIM(RTRIM(word))) PERSISTED,
    severity_level TINYINT NOT NULL CONSTRAINT DF_sw_words_severity DEFAULT (1),
    is_active BIT NOT NULL CONSTRAINT DF_sw_words_is_active DEFAULT (1),
    effective_from DATETIME2(3) NOT NULL CONSTRAINT DF_sw_words_effective_from DEFAULT (SYSUTCDATETIME()),
    created_at DATETIME2(3) NOT NULL CONSTRAINT DF_sw_words_created_at DEFAULT (SYSUTCDATETIME()),
    updated_at DATETIME2(3) NULL,
    created_by NVARCHAR(200) NOT NULL CONSTRAINT DF_sw_words_created_by DEFAULT (N'system'),
    updated_by NVARCHAR(200) NULL,
    CONSTRAINT CK_sw_words_severity
        CHECK (severity_level BETWEEN 1 AND 255)
);
GO

CREATE UNIQUE INDEX UX_sw_words_active_word
    ON sw.sensitive_words(normalized_word)
    WHERE is_active = 1;
GO

CREATE INDEX IX_sw_words_active
    ON sw.sensitive_words(is_active, severity_level);
GO

CREATE TABLE sw.sanitization_requests (
    request_id UNIQUEIDENTIFIER NOT NULL
        CONSTRAINT PK_sw_sanitization_requests PRIMARY KEY
        CONSTRAINT DF_sw_sanitization_requests_id DEFAULT (NEWID()),
    source_system NVARCHAR(100) NULL,
    original_text NVARCHAR(MAX) NOT NULL,
    sanitized_text NVARCHAR(MAX) NOT NULL,
    matched_words_count INT NOT NULL CONSTRAINT DF_sw_sanitization_requests_matches DEFAULT (0),
    processing_time_ms INT NULL,
    created_at DATETIME2(3) NOT NULL CONSTRAINT DF_sw_sanitization_requests_created_at DEFAULT (SYSUTCDATETIME())
);
GO

CREATE INDEX IX_sw_sanitization_requests_created_at
    ON sw.sanitization_requests(created_at DESC);
GO

CREATE TABLE sw.sensitive_word_audit_log (
    audit_id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    sensitive_word_id BIGINT NULL,
    action_type NVARCHAR(20) NOT NULL,
    old_value NVARCHAR(MAX) NULL,
    new_value NVARCHAR(MAX) NULL,
    changed_by NVARCHAR(200) NOT NULL CONSTRAINT DF_sw_audit_changed_by DEFAULT (N'system'),
    changed_at DATETIME2(3) NOT NULL CONSTRAINT DF_sw_audit_changed_at DEFAULT (SYSUTCDATETIME()),
    CONSTRAINT FK_sw_audit_sensitive_word
        FOREIGN KEY (sensitive_word_id) REFERENCES sw.sensitive_words(sensitive_word_id),
    CONSTRAINT CK_sw_audit_action_type
        CHECK (action_type IN (N'INSERT', N'UPDATE', N'DEACTIVATE'))
);
GO

CREATE INDEX IX_sw_audit_sensitive_word_changed_at
    ON sw.sensitive_word_audit_log(sensitive_word_id, changed_at DESC);
GO
