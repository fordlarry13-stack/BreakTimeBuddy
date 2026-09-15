# Break Time Buddy

Desktop app (Java + JavaFX) that tracks work sessions and recommends a short break. Recommendations come from Groq when `GROQ_API_KEY` is set, otherwise from a simple fallback.

## Requirements

- Java 17
- Maven

## Run

```bash
mvn clean javafx:run
```

## Test
mvn test

## Repo map
src/main/java/com/breaktimebuddy/ — app code
src/test/java/com/breaktimebuddy/ — tests
docs/architecture.md — system map
.github/workflows/ci.yml — CI on develop


## Working branch
Day-to-day work is on develop.
