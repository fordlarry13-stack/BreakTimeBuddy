# Break Time Buddy

Desktop app (Java + JavaFX) that tracks work sessions and recommends a short break. Recommendations come from Groq when `GROQ_API_KEY` is set, otherwise from a simple fallback.

Alpha limitation: the recommendation service exists, but it is not shown in the window yet.

## Requirements

- Java 17
- Maven

## Run
```bash
mvn clean javafx:run
```

## Test
```bash
mvn test
```

## AI configuration
```bash
export GROQ_API_KEY="key"
mvn clean javafx:run
```
If GROQ_API_KEY is not set, the app still runs and uses the rule-based fallback. No key is required.

## Repo map
`src/main/java/com/breaktimebuddy/` — app code
`src/test/java/com/breaktimebuddy/` — tests
`docs/architecture.md` — system map
`.github/workflows/ci.yml` — CI on `develop`


## Working branch
Day-to-day work is on develop.
