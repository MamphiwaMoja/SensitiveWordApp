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
      AND is_active = 1
)
BEGIN
    INSERT INTO sw.sensitive_words (
        category_id,
        word,
        severity_level,
        is_active,
        created_by
    )
    VALUES (
        @profanity_category_id,
        N'testbadword',
        3,
        1,
        N'seed'
    );
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sw.sensitive_words
    WHERE normalized_word = N'restricted phrase'
      AND is_active = 1
)
BEGIN
    INSERT INTO sw.sensitive_words (
        category_id,
        word,
        severity_level,
        is_active,
        created_by
    )
    VALUES (
        @compliance_category_id,
        N'restricted phrase',
        4,
        1,
        N'seed'
    );
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sw.sensitive_words
    WHERE normalized_word = N'scam'
      AND is_active = 1
)
BEGIN
    INSERT INTO sw.sensitive_words (
        category_id,
        word,
        severity_level,
        is_active,
        created_by
    )
    VALUES (
        @fraud_category_id,
        N'scam',
        5,
        1,
        N'seed'
    );
END;
GO
