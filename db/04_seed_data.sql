USE SensitiveWordsDb;
GO

SET NOCOUNT ON;
GO

IF NOT EXISTS (SELECT 1 FROM sw.sensitive_word_categories WHERE category_code = N'PROFANITY')
BEGIN
    INSERT INTO sw.sensitive_word_categories (category_code, category_name, description, created_by)
    VALUES (N'PROFANITY', N'Profanity', N'General profanity and abusive language.', N'seed');
END;
GO

IF NOT EXISTS (SELECT 1 FROM sw.sensitive_word_categories WHERE category_code = N'COMPLIANCE')
BEGIN
    INSERT INTO sw.sensitive_word_categories (category_code, category_name, description, created_by)
    VALUES (N'COMPLIANCE', N'Compliance', N'Terms that should be masked for compliance reasons.', N'seed');
END;
GO

IF NOT EXISTS (SELECT 1 FROM sw.sensitive_word_categories WHERE category_code = N'FRAUD')
BEGIN
    INSERT INTO sw.sensitive_word_categories (category_code, category_name, description, created_by)
    VALUES (N'FRAUD', N'Fraud', N'Fraud-risk words and phrases.', N'seed');
END;
GO

DECLARE @profanity_category_id BIGINT = (
    SELECT category_id
    FROM sw.sensitive_word_categories
    WHERE category_code = N'PROFANITY'
);

DECLARE @compliance_category_id BIGINT = (
    SELECT category_id
    FROM sw.sensitive_word_categories
    WHERE category_code = N'COMPLIANCE'
);

DECLARE @fraud_category_id BIGINT = (
    SELECT category_id
    FROM sw.sensitive_word_categories
    WHERE category_code = N'FRAUD'
);

IF NOT EXISTS (
    SELECT 1
    FROM sw.sensitive_words
    WHERE normalized_word = N'testbadword'
      AND match_type = N'CONTAINS'
      AND is_active = 1
)
BEGIN
    INSERT INTO sw.sensitive_words (
        category_id,
        word,
        replacement_value,
        match_type,
        severity_level,
        is_case_sensitive,
        is_active,
        notes,
        created_by
    )
    VALUES (
        @profanity_category_id,
        N'testbadword',
        N'******',
        N'CONTAINS',
        3,
        0,
        1,
        N'Default profanity sample for sanitization smoke tests.',
        N'seed'
    );
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sw.sensitive_words
    WHERE normalized_word = N'restricted phrase'
      AND match_type = N'EXACT'
      AND is_active = 1
)
BEGIN
    INSERT INTO sw.sensitive_words (
        category_id,
        word,
        replacement_value,
        match_type,
        severity_level,
        is_case_sensitive,
        is_active,
        notes,
        created_by
    )
    VALUES (
        @compliance_category_id,
        N'restricted phrase',
        N'[restricted]',
        N'EXACT',
        4,
        0,
        1,
        N'Phrase-level exact match sample.',
        N'seed'
    );
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sw.sensitive_words
    WHERE normalized_word = N'scam'
      AND match_type = N'CONTAINS'
      AND is_active = 1
)
BEGIN
    INSERT INTO sw.sensitive_words (
        category_id,
        word,
        replacement_value,
        match_type,
        severity_level,
        is_case_sensitive,
        is_active,
        notes,
        created_by
    )
    VALUES (
        @fraud_category_id,
        N'scam',
        N'[risk-term]',
        N'CONTAINS',
        5,
        0,
        1,
        N'Fraud keyword sample.',
        N'seed'
    );
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sw.sensitive_words
    WHERE word = N'\bID\d{4}\b'
      AND match_type = N'REGEX'
      AND is_active = 1
)
BEGIN
    INSERT INTO sw.sensitive_words (
        category_id,
        word,
        replacement_value,
        match_type,
        severity_level,
        is_case_sensitive,
        is_active,
        notes,
        created_by
    )
    VALUES (
        @compliance_category_id,
        N'\bID\d{4}\b',
        N'[identifier]',
        N'REGEX',
        2,
        0,
        1,
        N'Regex sample for identifier-like values.',
        N'seed'
    );
END;
GO
