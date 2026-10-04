# AI Recommendation Service

## Overview

Break Time Buddy includes an AI-assisted recommendation service that helps determine whether a user should take a break and, when appropriate, provides a short break activity.

The AI functionality is separated behind the `RecommendationService` interface. This keeps the recommendation logic modular and allows the AI provider or fallback implementation to be changed without requiring major changes to the rest of the application.

The current implementation uses Groq as the external AI provider and `FallbackRecommendationService` as a rule-based fallback.

## Architecture

The recommendation flow is:

```text
Active work session
        |
        v
RecommendationRequest
        |
        v
RecommendationService
        |
        v
GroqRecommendationService
        |
        v
Groq API
        |
        v
RecommendationResponse
   |               |
shouldBreak=false  shouldBreak=true
   |               |
No dialog          Display break recommendation
```

If the Groq service cannot be used or returns an invalid response, the application delegates to:

```text
FallbackRecommendationService
        |
        v
Rule-based break decision
        |
        v
RecommendationResponse
```

`GroqHttpClient` provides an abstraction around HTTP communication, while `DefaultGroqHttpClient` provides the runtime HTTP implementation. This separation allows automated tests to inject a fake HTTP client without making live network requests.

## AI provider configuration

The current Groq integration uses the following model:

```text
qwen/qwen3.8-27b
```

The Groq API key is read from the `GROQ_API_KEY` environment variable.

For local development on macOS or Linux:

```bash
export GROQ_API_KEY="your-api-key"
```

The actual API key must never be committed to the Git repository.

If the API key is missing or blank, the application immediately uses the fallback recommendation service rather than failing the recommendation feature.

## Recommendation input

`RecommendationRequest` provides the recommendation service with four pieces of session context:

- Number of completed work sessions.
- Preferred work-session length.
- Duration of the current work session.
- Recent completed work and break history.

Recent history includes the phase and duration of completed work and break sessions. This gives the recommendation service more context than relying on completed session count alone.

The application creates the request while a work session is active and passes a copy of the recent history to the recommendation service.

## Recommendation response

The recommendation service returns a `RecommendationResponse` containing:

- `shouldBreak` — whether a break should currently be recommended.
- `activity` — the recommended break activity when `shouldBreak` is `true`.

If `shouldBreak` is `false`, the application does not display a recommendation dialog.

If `shouldBreak` is `true`, the recommendation activity is displayed to the user. Accepting the recommendation ends the current work period and starts a break. Rejecting it dismisses the recommendation without starting a break.

This design allows the recommendation service to decide that a break is not yet appropriate instead of always displaying a break suggestion.

## Groq request and structured response

The Groq prompt includes:

- Completed work-session count.
- Preferred work-session length.
- Current continuous work duration.
- Recent completed work and break history.

The prompt asks the model to decide whether the user should take a break. When a break is appropriate, it requests one short, healthy activity under 30 words and instructs the model not to provide medical advice.

The Groq request uses a JSON schema requiring the `shouldBreak` Boolean field. The `activity` field is used when a break is recommended.

Model reasoning is configured as hidden.

## Response validation

AI-generated activities are validated before being displayed.

When `shouldBreak` is `true`, validation verifies that the activity:

- Is not null or blank.
- Contains no more than 30 words.
- Does not expose reasoning tags such as `<think>`.
- Does not contain configured medical-advice terms, including medication, dosage, diagnosis, treatment, prescription, or named pain-relief medications.

Malformed JSON, missing required activity data, or other invalid responses are rejected.

If an AI response requiring an activity fails validation, the application uses the fallback recommendation service instead of displaying the invalid response.

## Retry and error handling

The Groq service supports up to three total request attempts, including the initial attempt.

It retries transient failures including:

- HTTP 429 rate-limit responses.
- HTTP 5xx server responses.
- Network or asynchronous request failures.

Non-retryable HTTP responses, such as HTTP 401, fall back without performing the normal retry sequence.

The current implementation uses a one-second delay between retry attempts. Each HTTP request has a 10-second timeout.

After retry attempts are exhausted, the application delegates to `FallbackRecommendationService`. This prevents external AI-provider failures from breaking the recommendation feature.

## Rule-based fallback

`FallbackRecommendationService` allows the application to make a break decision without Groq.

The fallback compares the current work duration with the user's preferred work-session length. A break is recommended when the current working duration is greater than 80% of the preferred work length.

When a break is recommended, the activity varies according to the number of completed work sessions:

- Four or more completed sessions: a 10-minute break with walking, stretching, and water.
- Two or three completed sessions: a 5-minute stretching break.
- Fewer than two completed sessions: a short break to rest the eyes.

If the current work duration has not exceeded the threshold, the fallback returns `shouldBreak=false` and no break activity is displayed.

## Automated testing

Run the complete automated test suite from the project root:

```bash
mvn clean test
```

The final integrated `develop` build at commit `045053e` was verified on October 3, 2026 with:

```text
Tests run: 77
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

This represents a 100% passing test run.

AI and recommendation-related automated testing includes scenarios for:

- Structured recommendation responses.
- Break and no-break decisions.
- Invalid JSON responses.
- Missing or invalid activity data.
- Responses exceeding the 30-word limit.
- Medical-advice validation.
- Missing API key fallback.
- HTTP 429 retry behavior.
- HTTP 5xx retry behavior.
- Network and asynchronous failures.
- Non-retryable HTTP responses.
- Rule-based fallback behavior.
- Recommendation-service integration with the session flow.
- Recommendation acceptance and rejection.
- Failed and stale asynchronous recommendation handling.

The Groq unit tests use an injected fake HTTP client. Therefore, automated tests and CI do not require a real Groq API key or live Groq request.

## Manual AI testing

The recommendation flow can also be verified manually.

### Setup

Set the Groq API key locally:

```bash
export GROQ_API_KEY="your-api-key"
```

Do not place a real API key in source code, documentation, commits, or test files.

### Verification

While a work session is active, request a recommendation and verify the resulting behavior.

Because AI responses are nondeterministic, testing should validate behavior rather than require an exact sentence.

When the service decides a break is appropriate, verify that:

- A recommendation dialog is displayed.
- The activity is concise and non-empty.
- The activity does not exceed the configured word limit.
- The activity does not expose model reasoning.
- Accepting the recommendation starts a break.
- Rejecting the recommendation dismisses it.

When the service determines that a break is not appropriate, verify that no recommendation dialog is displayed.

The fallback path can be tested by running the application without `GROQ_API_KEY`.

## Reliability and graceful degradation

The recommendation feature is designed so that the external AI provider is not a single point of failure.

Fallback behavior can occur when:

- `GROQ_API_KEY` is unavailable.
- The provider returns an invalid or malformed response.
- A required break activity is missing or invalid.
- The response fails safety validation.
- A non-retryable HTTP error occurs.
- Retryable failures continue through all attempts.
- Network or asynchronous failures persist.

This allows the application to continue making break decisions even when the external AI service is unavailable.

## Technical debt and potential resolutions

### Direct desktop-to-AI communication

**Technical debt:**
The Java desktop application currently communicates directly with the Groq API.

**Impact:**
A distributed desktop application cannot securely protect a provider credential because secrets stored on a client device may be extracted.

**Potential resolution:**
Move AI communication to a backend or proxy service. The backend could securely store provider credentials while the JavaFX application communicates with the application's backend over HTTPS.

### Fixed retry delay

**Technical debt:**
The implementation uses a fixed one-second delay between retry attempts.

**Impact:**
A fixed delay may be inefficient during extended provider outages or rate limiting.

**Potential resolution:**
Use exponential backoff and honor provider retry guidance such as `Retry-After` when available.

### Client-side service protection

**Technical debt:**
The current architecture does not include a project-owned backend between the desktop client and external AI service.

**Impact:**
Server-side controls such as centralized request validation, rate limiting, credential isolation, and service-level monitoring cannot be fully implemented in the current client-only architecture.

**Potential resolution:**
Introduce a backend/proxy layer if the application progresses beyond the current desktop project architecture.

### AI recommendation validation

**Technical debt:**
The application validates response structure, length, exposed reasoning, and a configured set of medical-advice terms, but it cannot guarantee the semantic quality of every nondeterministic AI response.

**Impact:**
A syntactically valid response may still be less useful or relevant than expected.

**Potential resolution:**
Expand safety and relevance test cases, introduce additional semantic validation where appropriate, and continue evaluating recommendation quality using representative scenarios.

## Final release status

The final recommendation module provides:

- A modular `RecommendationService` abstraction.
- Groq AI integration.
- Conditional break/no-break decisions.
- Context from current duration, preferred work length, completed sessions, and recent history.
- Structured `RecommendationResponse` handling.
- Asynchronous HTTP communication.
- Environment-variable API key configuration.
- Structured JSON response parsing.
- Activity validation and medical-advice filtering.
- Retry and timeout handling.
- Rule-based graceful fallback.
- Automated testing without live external requests.
- Integration with the JavaFX session and recommendation-dialog lifecycle.

The recommendation service is integrated with the application's active work-session flow. A recommendation dialog is created only when the resulting `RecommendationResponse` indicates that a break should be taken. Provider failures and invalid responses degrade to deterministic fallback behavior rather than disabling the recommendation feature.