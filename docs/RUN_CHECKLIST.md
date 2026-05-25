# Local Run Checklist

Use this checklist before running the Spring Boot application.

## 1. Software checklist

| Requirement | Needed? | How to check |
|---|---:|---|
| Java 17 or later | Yes | `java -version` |
| Maven 3.8+ | Yes | `mvn -version` |
| SQL Server running | Yes | Connect using SSMS |
| SSMS | Helpful | You already installed/used it |
| Database created | Yes | `SELECT DB_NAME();` inside `SensitiveWordsDb` |
| Seed data loaded | Yes | Run the check queries from earlier |
| TCP/IP enabled for SQL Server | Usually yes | SQL Server Configuration Manager |
| Port 1433 reachable | Usually yes | Check SQL Server network config |

## 2. Database checklist

Run the setup scripts in this order:

```text
db/01_create_database.sql
db/02_create_schema.sql
db/03_create_tables.sql
db/04_seed_data.sql
db/05_test_connection_and_queries.sql
```

Then confirm the database exists:

```sql
SELECT name FROM sys.databases WHERE name = 'SensitiveWordsDb';
```

Confirm the schema exists:

```sql
USE SensitiveWordsDb;
SELECT name FROM sys.schemas WHERE name = 'sw';
```

Confirm tables exist:

```sql
USE SensitiveWordsDb;
SELECT TABLE_SCHEMA, TABLE_NAME
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'sw'
ORDER BY TABLE_NAME;
```

Confirm seed data exists:

```sql
USE SensitiveWordsDb;
SELECT COUNT(*) AS total_sensitive_words
FROM sw.sensitive_words;
```

Expected: count should be greater than zero.

## 3. SQL login checklist

The app needs one of these:

- SQL Server username/password, recommended for local app testing
- Windows authentication, more complex from Java depending on your environment

Recommended local option:

```sql
USE master;
SELECT name FROM sys.sql_logins WHERE name = 'sensitive_words_app';
```

If it does not exist, run:

```text
db/06_create_app_login_optional.sql
```

## 4. Application config checklist

The local profile expects these values:

```text
DB_URL=jdbc:sqlserver://localhost:1433;databaseName=SensitiveWordsDb;encrypt=true;trustServerCertificate=true
DB_USERNAME=sensitive_words_app
DB_PASSWORD=ChangeMe!12345
```

You can either:

- set environment variables, or
- edit `src/main/resources/application-local.yml` temporarily for local testing

Do not commit real passwords.

## 5. Run checklist

From the project root:

```bash
mvn clean test
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Expected logs should show:

```text
Started SensitiveWordsServiceApplication
```

## 6. Browser checks

Open these URLs:

```text
http://localhost:8080/actuator/health
http://localhost:8080/swagger-ui/index.html
```

## 7. API smoke test

```bash
curl -X POST http://localhost:8080/api/v1/sanitize \
  -H "Content-Type: application/json" \
  -d '{"inputText":"This contains testbadword.","sourceSystem":"local-check"}'
```

Expected:

```text
sanitizedText contains ***
matchedWordsCount is greater than 0
```

## 8. Common issues

### Login failed for user

Your username/password is wrong, the login does not exist, or SQL authentication is disabled.

### Connection refused

SQL Server is not running, TCP/IP is disabled, or the server is not listening on port `1433`.

### Database not found

The database scripts were not run against this SQL Server instance.

### Table not found

Confirm your schema is `sw` and that the scripts created the tables in `SensitiveWordsDb`.
