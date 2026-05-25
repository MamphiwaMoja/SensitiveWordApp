@echo off
setlocal

REM Update these values if your SQL Server login/password differs.
set DB_URL=jdbc:sqlserver://localhost:1433;databaseName=SensitiveWordsDb;encrypt=true;trustServerCertificate=true
set DB_USERNAME=sensitive_words_app
set DB_PASSWORD=ChangeMe!12345

mvn spring-boot:run -Dspring-boot.run.profiles=local
