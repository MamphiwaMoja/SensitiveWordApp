USE SensitiveWordsDb;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.schemas
    WHERE name = N'sw'
)
BEGIN
    EXEC('CREATE SCHEMA sw');
END;
GO

SELECT name AS schema_name
FROM sys.schemas
WHERE name = N'sw';
GO
