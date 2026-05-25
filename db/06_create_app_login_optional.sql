/*
    06_create_app_login_optional.sql
    Purpose: Create a local SQL Server login/user for the Spring Boot app.

    Run this only for local development.
    Change the password before using this for anything serious.
*/

USE [master];
GO

IF NOT EXISTS (SELECT 1 FROM sys.sql_logins WHERE name = 'sensitive_words_app')
BEGIN
    CREATE LOGIN [sensitive_words_app]
    WITH PASSWORD = 'ChangeMe!12345',
         CHECK_POLICY = ON,
         CHECK_EXPIRATION = OFF;
END
GO

USE [SensitiveWordsDb];
GO

IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = 'sensitive_words_app')
BEGIN
    CREATE USER [sensitive_words_app] FOR LOGIN [sensitive_words_app];
END
GO

GRANT SELECT, INSERT, UPDATE, DELETE ON SCHEMA::sw TO [sensitive_words_app];
GRANT EXECUTE ON SCHEMA::sw TO [sensitive_words_app];
GO

SELECT
    'OK' AS status,
    'sensitive_words_app login/user is ready' AS message;
GO
