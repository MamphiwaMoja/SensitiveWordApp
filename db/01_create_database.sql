IF DB_ID(N'SensitiveWordsDb') IS NULL
BEGIN
    CREATE DATABASE SensitiveWordsDb;
END;
GO

USE SensitiveWordsDb;
GO

SELECT DB_NAME() AS current_database;
GO
