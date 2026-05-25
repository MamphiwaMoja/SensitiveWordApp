USE SensitiveWordsDb;
GO

SELECT DB_NAME() AS current_database;
GO

SELECT TABLE_SCHEMA, TABLE_NAME
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = N'sw'
ORDER BY TABLE_NAME;
GO

SELECT
    COUNT(*) AS total_categories
FROM sw.sensitive_word_categories;
GO

SELECT
    COUNT(*) AS total_sensitive_words,
    SUM(CASE WHEN is_active = 1 THEN 1 ELSE 0 END) AS active_sensitive_words
FROM sw.sensitive_words;
GO

SELECT TOP (20)
    sensitive_word_id,
    word,
    normalized_word,
    severity_level,
    is_active
FROM sw.sensitive_words
ORDER BY severity_level DESC, word ASC;
GO
