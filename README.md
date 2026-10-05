# Break Time Buddy

Break Time Buddy is a Java 17 and JavaFX desktop application designed to help users manage work and break sessions. The application tracks completed sessions, maintains local history and preferences, and provides break recommendations through an optional Groq-powered recommendation service with a rule-based fallback.

The project was developed as part of the UMGC CMSC 495 Computer Science Capstone.

## Features

- Start and end work and break sessions manually.
- Track the number of completed work sessions.
- Set a preferred work-session length from 1 to 300 minutes.
- Maintain up to 20 completed work and break history entries.
- Save and load supported configuration data locally.
- Request break recommendations during an active work session.
- Use Groq for optional AI-assisted recommendations.
- Continue providing rule-based recommendations when Groq is unavailable or not configured.
- Validate AI responses before presenting recommendations.
- Handle recommendation requests asynchronously.
- Retry selected transient AI-provider failures.
- Run automated tests through Maven and GitHub Actions.

## Technology Stack

- **Language:** Java 17
- **Desktop UI:** JavaFX 17
- **Build and dependency management:** Maven
- **JSON processing:** Gson
- **Testing:** JUnit 5
- **AI provider:** Groq (optional)
- **Continuous integration:** GitHub Actions

## Requirements

Install the following before running the application:

- Java Development Kit (JDK) 17 or a compatible environment capable of building the Java 17 target
- Maven
- Git

Groq access is optional. The application can operate with its rule-based fallback when `GROQ_API_KEY` is not configured.

## Getting Started

Clone the team repository:

```bash
git clone https://github.com/fordlarry13-stack/BreakTimeBuddy.git
cd BreakTimeBuddy
```

During active development, the integrated application is maintained on the `develop` branch:

```bash
git switch develop
```

Run the application:

```bash
mvn clean javafx:run
```

## Optional Groq Configuration

To enable Groq-backed recommendations on macOS or Linux, set the API key in the environment before launching the application:

```bash
export GROQ_API_KEY="your-api-key"
mvn clean javafx:run
```

Do not commit a real API key to the repository.

If `GROQ_API_KEY` is missing, blank, or the Groq service cannot return a valid recommendation, the application uses its rule-based fallback.

Using an environment variable reduces the risk of accidentally committing the credential to source control, but it is not production-grade secret protection for a distributed desktop application.

## Basic Usage

1. Launch Break Time Buddy.
2. Set the preferred work-session length if desired.
3. Start a work session using the session toggle.
4. Request a break recommendation while a work session is active.
5. Respond to a displayed recommendation by starting a break or dismissing it.
6. Continue toggling between work and break periods as needed.
7. Review completed periods in the session history.
8. Save the supported local configuration when desired.

Session changes are controlled manually. Break Time Buddy does not perform automatic idle detection or background activity monitoring.

## AI Recommendation Flow

The recommendation functionality is isolated behind the `RecommendationService` interface.

At a high level:

```text
JavaFX UI
    |
    v
Interactor
    |
    v
RecommendationService
    |
    +----> GroqRecommendationService ----> Groq API
    |
    +----> FallbackRecommendationService
```

When Groq is configured, the application can request an AI-generated recommendation. The recommendation service returns a `RecommendationResponse` containing a `shouldBreak` decision and an activity when a break is recommended. If `shouldBreak` is `false`, the application does not display a break recommendation dialog.

AI responses are validated before they are used by the application. The fallback service allows the recommendation feature to continue operating when the AI provider is unavailable, not configured, or does not return an acceptable response.

The Groq integration also includes bounded retry behavior for selected transient failures and request timeouts so provider failures do not indefinitely block the recommendation workflow.

## AI and Wellness Disclosure

When Groq is configured, recommendation requests may include session-related information such as work-session progress, preferences, and recent completed session information.

AI-generated recommendations can be inaccurate or unsuitable. Break Time Buddy's recommendations are intended as general break and wellness suggestions and are not medical advice.

The application also supports a non-AI rule-based fallback, so a live AI provider is not required for the core recommendation workflow.

## Local Data

Break Time Buddy stores supported configuration information locally in `config.json`.

Saved data includes the following:

- Completed work-session count
- Preferred work-session length
- Completed work and break history

The Groq API key is **not** stored in `config.json`. It is read from the `GROQ_API_KEY` environment variable when the application starts.

There are currently no user accounts or cloud-based configuration storage.

## Testing

Run the automated test suite with:

```bash
mvn clean test
```

The test suite covers the following areas:

- Configuration validation and persistence
- Work and break history
- Session and recommendation interaction logic
- User preferences
- Rule-based fallback behavior
- Groq response handling and validation
- Retry and error-handling behavior
- Missing-key fallback behavior
- Asynchronous and stale recommendation handling

AI-related automated tests use controlled test implementations rather than requiring a live Groq API request, allowing the automated suite and CI pipeline to run without a real provider credential.

Final integrated verification on October 3, 2026 completed **77 tests with 0 failures, 0 errors, and 0 skipped tests**.

For a full Maven verification build, run the following command:

```bash
mvn -B verify
```

## Packaging

Create the application artifacts with the following command:

```bash
mvn clean package
```

Maven generates build output under the `target/` directory, including the project JAR and an assembled JAR with dependencies.

## Continuous Integration

The repository includes a GitHub Actions Java CI workflow. For pushes and pull requests targeting `develop`, it runs the following processes:

1. Check out the repository.
2. Configure Temurin Java 17.
3. Use Maven dependency caching.
4. Run `mvn -B verify`.

Pull requests must pass the CI workflow before integration into the shared development branch.

## Repository Structure

```text
BreakTimeBuddy/
├── .github/
│   └── workflows/
│       └── ci.yml
├── docs/
│   ├── ai-recommendation-service.md
│   └── architecture.md
├── src/
│   ├── main/
│   │   └── java/com/breaktimebuddy/
│   └── test/
│       └── java/com/breaktimebuddy/
├── pom.xml
└── README.md
```

Additional final-release documentation is being maintained under `docs/`.

## Architecture

The application separates user-interface, interaction, persistence, and recommendation responsibilities.

Key components include:

- `ViewBuilder` and related view classes for the JavaFX interface.
- `Interactor` for session and recommendation workflow coordination.
- `ConfigHandler`, `FileStorage`, and related classes for local configuration persistence.
- `RecommendationService` as the recommendation abstraction.
- `GroqRecommendationService` for optional AI-provider integration.
- `FallbackRecommendationService` for rule-based recommendations.
- `GroqHttpClient` and `DefaultGroqHttpClient` for the HTTP abstraction and implementation.

This separation allows recommendation logic to be tested independently and provides a path for replacing or relocating the external AI integration in a future architecture.

## Security and Technical Limitations

Important current limitations include:

- Environment-variable API-key handling is appropriate for local development but is not production-grade secret protection for a distributed desktop application.
- Configuration is stored locally.
- Session transitions require manual user interaction.
- The application does not provide user accounts or cloud synchronization.
- External AI availability and response quality cannot be guaranteed.

A future production architecture could place AI communication behind a backend or proxy service that owns provider credentials and can add centralized validation, request controls, and rate limiting.

## Documentation

Project documentation is maintained in the `docs/` directory.

Final-release documentation includes:

- `docs/architecture.md` — architecture and component information.
- `docs/ai-recommendation-service.md` — AI recommendation implementation, validation, fallback, and testing information.
- `docs/api-documentation.md` — API documentation guidelines.
- `docs/INSTALLATION.md` — installation, setup, and application launch instructions.
- `docs/DEVELOPER_GUIDE.md` — development environment and contribution guidance.
- `docs/USER_MANUAL.md` — end-user setup, usage, recommendation, configuration, and troubleshooting guidance.

## Team

Break Time Buddy was developed by the CMSC 495 capstone team:

- **Larry Ford — Lead Architect**
- **Leora Greenlinger — UI/UX and Visual Design Lead**
- **Narumi Yokobori — Interface Designer**
- **Kapil Dhami — Integration Lead**

Development uses feature branches, pull requests, automated CI checks, peer review, and the shared `develop` integration branch.

## Project Status

Break Time Buddy is being finalized as the team deliverable for the UMGC CMSC 495 Computer Science Capstone.

Final-release verification includes integration testing, documentation review, AI/fallback validation, CI verification, and final release preparation.