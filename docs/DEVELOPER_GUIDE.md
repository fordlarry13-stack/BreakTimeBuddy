# Break Time Buddy — Developer guide

This guide contains development and contributor information for Break Time Buddy.

## Development prerequisites

Install the following before working with the project:

- Java 17
- Apache Maven
- Git

Use `java -version` and `mvn -version` to verify that Maven is running with a Java installation compatible with the Java 17 target. See [INSTALLATION.md](INSTALLATION.md) for project setup and application launch instructions.

## Running automated tests

Run the automated test suite from the project root:

```bash
mvn test
```

Maven compiles the required project and test sources and executes the configured test suite. The tests do not require a real Groq API key or a live Groq network request because the Groq tests use an injected fake HTTP client.

## Building and verifying changes

Perform a clean build and package the project with:

```bash
mvn clean package
```

Run the same Maven verification command configured in the GitHub Actions Java CI workflow with:

```bash
mvn -B verify
```

Build output is produced under `target/`. These commands do not imply that a platform-specific native installer is configured or verified.

## Groq API key security

Treat the Groq API key as a secret.

- Never commit `GROQ_API_KEY` to Git or GitHub.
- Never hardcode the API key in Java source code.
- Never store the API key in `config.json`.
- Configure the credential through the `GROQ_API_KEY` environment variable.

Using an environment variable reduces the risk of accidentally committing a credential to source control. However, Break Time Buddy is currently a desktop client that communicates with the AI provider, so environment-variable configuration should not be considered production-grade secret protection for a distributed desktop application.

A server-side/backend proxy architecture may be considered as a future security improvement, but it is not part of the current installation or application architecture.

## Contributor security practices

- Do not expose a Groq API key in screenshots, logs, documentation, presentations, or other shared materials.
- Use placeholders rather than real credentials in examples.
- Keep `GROQ_API_KEY` optional so the application can use its rule-based fallback when Groq is not configured.
- Do not add credentials to source code, tests, configuration files, or repository history.
