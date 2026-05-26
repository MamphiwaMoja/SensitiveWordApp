# Swagger API Guide

This guide gives reviewers a quick path through the documented REST API.

## OpenAPI URLs

Start the app locally, then open:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Checked-in OpenAPI contract: `http://localhost:8080/swagger/sensitive-words-service.yaml`
- Generated Springdoc contract: `http://localhost:8080/v3/api-docs.yaml`
- Hosted Swagger UI: `https://mamphiwamoja.github.io/SensitiveWordApp/`
- Source OpenAPI contract: `src/main/resources/swagger/sensitive-words-service.yaml`

Local Swagger UI is configured to load the source contract from `src/main/resources/swagger/sensitive-words-service.yaml` so the API documentation remains stable for review. GitHub Pages serves [docs/index.html](index.html), which loads the same source contract from the repository. The generated contract is still available for comparison.

## External Business Endpoint

`POST /api/v1/sanitize`

This is the externally consumed business endpoint from the assessment scenario. It accepts text from a client system, checks it against database-backed sensitive words, and returns the amended text.

Example request:

```json
{
  "inputText": "hello On SELECT from accounts",
  "sourceSystem": "reviewer",
  "persistRequest": false
}
```

Expected response shape:

```json
{
  "requestId": null,
  "originalText": "hello On SELECT from accounts",
  "sanitizedText": "hello *** *** from accounts",
  "matchedWordsCount": 2,
  "processingTimeMs": 3,
  "matchedWords": [
    {
      "sensitiveWordId": 1,
      "word": "ON",
      "severityLevel": 1,
      "replacementCount": 1
    },
    {
      "sensitiveWordId": 2,
      "word": "SELECT",
      "severityLevel": 1,
      "replacementCount": 1
    }
  ]
}
```

## Internal CRUD Endpoints

These endpoints manage the sensitive-word table and should be restricted to internal/admin access outside local development.

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/api/v1/sensitive-words` | List sensitive words |
| `GET` | `/api/v1/sensitive-words/{id}` | Get one sensitive word |
| `POST` | `/api/v1/sensitive-words` | Create a sensitive word |
| `PATCH` | `/api/v1/sensitive-words/{id}` | Update selected fields |
| `DELETE` | `/api/v1/sensitive-words/{id}` | Permanently delete a sensitive word |

Create example:

```json
{
  "word": "REVIEW_TERM",
  "severityLevel": 2
}
```

Patch example:

```json
{
  "severityLevel": 3
}
```

## Health

Use `GET /actuator/health` for platform checks.

The custom `GET /api/v1/health` endpoint is documented for API-level smoke testing and returns the service name, status, and timestamp.
