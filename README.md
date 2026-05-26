# Sensitive Words Service

![Coverage](.github/badges/coverage.svg)

Spring Boot microservice for managing sensitive-word rules and sanitizing incoming text.

## Stack

- Java 17
- Spring Boot 3.3
- Spring Data JPA
- MSSQL
- Swagger / OpenAPI
- JUnit 5 + Mockito

## What the service does

1. Manages sensitive words through a REST API
2. Replaces active database words in incoming text with `***`
3. Tracks each matched word and replacement count in the sanitize response
4. Tracks CRUD audit events and optional sanitization request logs internally

## API overview

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/api/v1/sensitive-words` | List sensitive-word rules |
| `GET` | `/api/v1/sensitive-words/{id}` | Get one sensitive-word rule |
| `POST` | `/api/v1/sensitive-words` | Create a sensitive-word rule |
| `PATCH` | `/api/v1/sensitive-words/{id}` | Partially update a sensitive-word rule |
| `DELETE` | `/api/v1/sensitive-words/{id}` | Permanently delete a sensitive-word rule |
| `POST` | `/api/v1/sanitize` | Sanitize input text |
| `GET` | `/swagger/sensitive-words-service.yaml` | Checked-in OpenAPI contract |
| `GET` | `/v3/api-docs.yaml` | Generated OpenAPI document |
| `GET` | `/swagger-ui/index.html` | Swagger UI |
| `GET` | `/actuator/health` | Actuator health |

## Local setup

### Runtime profiles

| Profile | Intended use | Database setup | Authentication |
|---|---|---|---|
| `local` | Developer machine and Docker Compose | Local SQL bootstrap scripts | Disabled for all routes |
| `dev` | Shared development environment | Liquibase-managed migrations by default | HTTP Basic required |
| `test` | Automated tests and integration testing | Liquibase with test-provided datasource | HTTP Basic required |
| `staging` | Production-like validation | Liquibase-managed migrations | HTTP Basic required |
| `prod` | Production runtime | Liquibase-managed migrations | HTTP Basic required |

Maven profiles map directly to Spring Boot runtime profiles:

```bash
mvn spring-boot:run -Plocal
mvn spring-boot:run -Pdev
mvn spring-boot:run -Pstaging
mvn spring-boot:run -Pprod
```

The packaged Docker image defaults to `SPRING_PROFILES_ACTIVE=prod`. Docker Compose overrides this to `local`.

### 1. Fastest reviewer path

If Docker Desktop is available, reviewers can start the full assessment stack with one command:

```bash
docker compose up --build
```

What this does:

- starts SQL Server in a container
- runs the local database bootstrap scripts automatically
- builds the Spring Boot app image
- starts the app on port `8080`

Useful URLs after startup:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI contract: `http://localhost:8080/swagger/sensitive-words-service.yaml`
- Generated OpenAPI YAML: `http://localhost:8080/v3/api-docs.yaml`
- Actuator health: `http://localhost:8080/actuator/health`

To stop everything and remove containers:

```bash
docker compose down
```

To reset the stack completely and force a fresh database:

```bash
docker compose down -v
docker compose up --build
```

### 2. Manual prerequisites

- Java 17+
- Maven 3.8+
- SQL Server running locally or remotely
- SSMS or `sqlcmd` for database setup

### 3. Create the database and schema

Liquibase is enabled by default for `dev`, `staging`, `prod`, and `test`. It creates the `sw` schema, tables, indexes, and seed rows when the configured database user has DDL permissions.

Liquibase is disabled for `local` because the Docker and manual local paths use SQL bootstrap scripts with a least-privilege application login.

For the documented local setup, the app uses the least-privilege `sensitive_words_app` login. That user is granted access after the schema exists, so run these bootstrap scripts first:

```text
db/01_create_database.sql
db/02_create_schema.sql
db/03_create_tables.sql
db/04_seed_data.sql
```

Optional local SQL login setup:

```text
db/06_create_app_login_optional.sql
```

Optional verification queries:

```text
db/05_test_connection_and_queries.sql
```

If you instead run the app with a database user that has migration privileges, only the database itself must exist before startup. In that mode, run `db/01_create_database.sql`, configure the privileged user, and let Liquibase apply `src/main/resources/config/liquibase/master.xml`.

The local bootstrap scripts create:

- database: `SensitiveWordsDb`
- schema: `sw`
- sensitive-word rules table
- audit log table
- sanitization request log table
- seed SQL keyword data for smoke testing

### 4. Configure database access

The `local` profile reads these environment variables:

```text
DB_URL=jdbc:sqlserver://localhost:14333;databaseName=SensitiveWordsDb;encrypt=true;trustServerCertificate=true
DB_USERNAME=sensitive_words_app
DB_PASSWORD=SwApp.Local#2026
```

`dev`, `staging`, and `prod` require the same database variables without source-controlled defaults:

```text
DB_URL=jdbc:sqlserver://<host>:1433;databaseName=SensitiveWordsDb;encrypt=true;trustServerCertificate=true
DB_USERNAME=<database-user>
DB_PASSWORD=<database-password>
```

Example in PowerShell:

```powershell
$env:DB_URL="jdbc:sqlserver://localhost:14333;databaseName=SensitiveWordsDb;encrypt=true;trustServerCertificate=true"
$env:DB_USERNAME="sensitive_words_app"
$env:DB_PASSWORD="SwApp.Local#2026"
```

### 5. Configure API security

The `local` profile permits API requests without authentication for local development.

Any non-`local` profile requires HTTP Basic authentication for API routes. Health endpoints remain public. Configure the credentials through environment variables:

```text
SECURITY_BASIC_USERNAME=sensitive-words
SECURITY_BASIC_PASSWORD=<set-a-secret>
SECURITY_BASIC_ROLE=API_USER
```

Example authenticated request for non-local profiles:

```bash
curl -u sensitive-words:<set-a-secret> http://localhost:8080/api/v1/sensitive-words
```

### 6. Build and run

```bash
mvn clean test
mvn spring-boot:run -Plocal
```

### 7. Open the application

- Health: `http://localhost:8080/actuator/health`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI contract: `http://localhost:8080/swagger/sensitive-words-service.yaml`
- Generated OpenAPI YAML: `http://localhost:8080/v3/api-docs.yaml`

Swagger UI now loads the checked-in OpenAPI contract from `src/main/resources/swagger/sensitive-words-service.yaml`. The generated Springdoc endpoints remain available for comparison and tooling.

See [Swagger API Guide](docs/SWAGGER_API.md) for a reviewer-friendly walkthrough of the documented endpoints and example payloads.

## API Documentation

The API is documented with OpenAPI and Swagger.

- Local Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Local checked-in OpenAPI YAML: `http://localhost:8080/swagger/sensitive-words-service.yaml`
- Local generated OpenAPI YAML: `http://localhost:8080/v3/api-docs.yaml`
- Hosted Swagger UI: `https://mamphiwamoja.github.io/SensitiveWordApp/`

The source OpenAPI contract lives at [src/main/resources/swagger/sensitive-words-service.yaml](src/main/resources/swagger/sensitive-words-service.yaml). GitHub Pages serves [docs/index.html](docs/index.html), which loads that same source contract from the repository.

Additional reviewer docs:

- [Swagger API Guide](docs/SWAGGER_API.md) shows the external sanitize endpoint, internal CRUD endpoints, and example payloads.

## Example API calls

### Sanitize text

```bash
curl -X POST http://localhost:8080/api/v1/sanitize \
  -H "Content-Type: application/json" \
  -d '{"inputText":"hello On SELECT from accounts","sourceSystem":"curl-test"}'
```

Request persistence is now opt-in. To persist the request:

```bash
curl -X POST http://localhost:8080/api/v1/sanitize \
  -H "Content-Type: application/json" \
  -d '{"inputText":"Possible DROP and UPDATE message","sourceSystem":"curl-test","persistRequest":true}'
```

### Create a sensitive word

```bash
curl -X POST http://localhost:8080/api/v1/sensitive-words \
  -H "Content-Type: application/json" \
  -d '{"word":"local-demo-term","severityLevel":2,"active":true}'
```

### Update a sensitive word

```bash
curl -X PATCH http://localhost:8080/api/v1/sensitive-words/1 \
  -H "Content-Type: application/json" \
  -d '{"severityLevel":3}'
```

### Delete a sensitive word

```bash
curl -X DELETE http://localhost:8080/api/v1/sensitive-words/1
```

## Design notes

- Layered structure: controller -> service -> repository -> MSSQL
- DTOs are used for request and response contracts
- DELETE physically removes the sensitive-word row; audit snapshots are retained separately
- Active sensitive words are cached in memory and invalidated after CRUD changes
- Active-word lookup orders longer entries first so overlapping phrases replace predictably
- Sanitization uses literal, case-insensitive word replacement with the constant `***`
- Request payload persistence is disabled by default to reduce risk around sensitive content
- Hibernate DDL generation is disabled; schema migrations live in Liquibase, while the SQL scripts support local least-privilege and Docker bootstrap paths
- Environment-specific config lives in `src/main/resources/config/application-<profile>.yml`
- Non-local profiles require HTTP Basic authentication for API routes, while health endpoints remain public

## Production deployment walkthrough

1. Build and test the release candidate in CI.
   - Run unit tests, integration tests, coverage, and packaging.
   - Publish the Docker image only after the pipeline is green.

2. Provision production infrastructure.
   - Run the Spring Boot service on Kubernetes, ECS, Azure Container Apps, or another container platform.
   - Place the service behind an API gateway or load balancer.
   - Use a managed SQL Server instance or a highly available SQL Server deployment instead of a local container.

3. Configure runtime secrets and profiles.
   - Run the app with `SPRING_PROFILES_ACTIVE=prod`.
   - Provide `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `SECURITY_BASIC_USERNAME`, and `SECURITY_BASIC_PASSWORD` from a secrets manager.
   - Do not store real production secrets in source control or container images.

4. Apply database migrations.
   - Keep Hibernate DDL generation disabled.
   - Let Liquibase apply `src/main/resources/config/liquibase/master.xml` during deployment, or run the same changelog as a controlled pre-deployment migration step.
   - Verify that the `sw.sensitive_words` table contains the seeded SQL keyword data before opening traffic.

5. Expose the correct API surface.
   - Expose `POST /api/v1/sanitize` to client systems through the gateway.
   - Restrict CRUD endpoints under `/api/v1/sensitive-words` to internal admin users, a VPN, or an admin-only gateway route.
   - Keep `/actuator/health` available to the platform for readiness and liveness checks.

6. Operate safely.
   - Use rolling deployments so new instances become healthy before old instances are removed.
   - Enable central logging, metrics, tracing, and alerts for latency, error rate, database failures, and circuit breaker state.
   - Set request-size and rate limits at the gateway because the sanitize endpoint is externally consumed.
   - Keep request-body persistence disabled by default unless there is an explicit audit requirement.

7. Roll back if needed.
   - Roll back the container image through the deployment platform if application behavior regresses.
   - Treat database rollbacks separately because schema/data migrations may not be safely reversible after production writes.

## Performance considerations

Current implementation:

1. Cache active sensitive words in memory and invalidate on CRUD changes
2. Keep filtered indexes on active words and lookup columns
3. Prefer longest-word ordering when active words overlap
4. Avoid persisting sanitization request bodies unless explicitly needed

Remaining production-scale improvements:

1. Add metrics around request latency, match counts, cache hit ratio, and database timings
2. Add pagination response DTOs instead of serializing Spring `Page` directly
3. Add rate limiting or request-size controls at the gateway for public sanitization traffic

## Additional enhancements for future work

1. Role-based authorization so CRUD endpoints can be limited to admin users
2. Actor-aware audit attribution instead of the fixed `system` actor
3. Actuator metrics export to Prometheus/Grafana
4. Separate admin and runtime APIs if rule management and sanitization traffic scale independently

## Test status

Run locally with:

```bash
mvn test jacoco:report
```

The JaCoCo HTML coverage report is generated at `target/site/jacoco/index.html`.

The test suite covers:

- sanitization behavior
- controller validation and error handling
- CRUD service behavior
- literal word replacement behavior
