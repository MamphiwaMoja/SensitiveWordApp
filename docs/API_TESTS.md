# API Test Commands

Run these once the app is started locally.

## List sensitive words

```bash
curl http://localhost:8080/api/v1/sensitive-words
```

## Sanitize text

```bash
curl -X POST http://localhost:8080/api/v1/sanitize \
  -H "Content-Type: application/json" \
  -d '{"inputText":"This message contains testbadword and a restricted phrase.","sourceSystem":"curl-test"}'
```

## Create a sensitive word

```bash
curl -X POST http://localhost:8080/api/v1/sensitive-words \
  -H "Content-Type: application/json" \
  -d '{"word":"local-demo-term","replacementValue":"[removed]","matchType":"CONTAINS","severityLevel":2,"caseSensitive":false,"active":true,"notes":"Created from curl smoke test"}'
```

## Update a sensitive word

Replace `{id}` with an actual ID returned from the list endpoint.

```bash
curl -X PATCH http://localhost:8080/api/v1/sensitive-words/{id} \
  -H "Content-Type: application/json" \
  -d '{"replacementValue":"***","severityLevel":3,"notes":"Updated from curl smoke test"}'
```

## Deactivate a sensitive word

```bash
curl -X DELETE http://localhost:8080/api/v1/sensitive-words/{id}
```
