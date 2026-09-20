# AI Recommendation Service

## Overview

The Break Time Buddy Alpha includes a client-side AI recommendation service that provides short break recommendations based on the number of completed work sessions.

The AI functionality is separated behind the `RecommendationService` interface. This keeps the implementation modular and allows the AI provider to be changed later without requiring major changes to the rest of the application.

For the Alpha release, Groq is used as the AI provider.

## Architecture

The current AI recommendation flow is:

```text
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
```

If the Groq request fails or the response is invalid, the application uses:

```text
FallbackRecommendationService
        |
        v
Rule-based break recommendation
```

`GroqHttpClient` provides an abstraction around the HTTP request. `DefaultGroqHttpClient` provides the production HTTP implementation. This separation allows automated tests to use a fake HTTP client without making real network requests.

## AI Provider Configuration

The Alpha implementation uses Groq with the following model:

```text
qwen/qwen3.8
```

The Groq API key is read from the `GROQ_API_KEY` environment variable.

For local development:

```bash
export GROQ_API_KEY="your-api-key"
```

The actual API key must never be committed to the Git repository.

If the API key is missing, the recommendation service uses the fallback service instead of causing the application to fail.

## Recommendation Input

The current `RecommendationRequest` contains the number of completed work sessions.

The session count provides the recommendation service with a factor that can be used when generating an appropriate break recommendation.

Additional factors can be added in future versions.

## Response Validation

AI responses are validated before they are returned to the application.

The current validation checks that:

- The recommendation is not null or blank.
- The recommendation is no more than 30 words.
- Exposed reasoning tags such as `<think>` are rejected.
- Invalid JSON responses do not cause the application to crash.

If validation fails, the fallback recommendation service is used.

## Retry and Error Handling

The Groq service uses up to three total attempts, including the initial request.

The service retries transient failures including:

- HTTP 429 rate-limit responses.
- HTTP 5xx server responses.
- Network or asynchronous request failures.

Non-transient responses such as HTTP 401 are not retried.

A one-second delay is currently used between retry attempts.

Each HTTP request has a 10-second timeout. The limited number of attempts was selected to keep failure handling within the project's performance target while still providing retry behavior.

## Automated Testing Procedure

Run the complete automated test suite from the project root:

```bash
mvn clean test
```

During Alpha development, the verified result was:

```text
Tests run: 34
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

The AI-related automated tests cover:

- Valid AI response handling.
- Invalid JSON handling.
- Responses exceeding the 30-word limit.
- Missing API key fallback.
- HTTP 429 retry behavior.
- HTTP 500 retry behavior.
- Network failure retry behavior.
- HTTP 401 no-retry behavior.
- Rule-based fallback recommendations.
- Recommendation service invocation from the session flow.
- Successful recommendation updates to the existing dialog state.
- Failed and stale asynchronous recommendation handling.

The Groq unit tests use an injected fake HTTP client. Therefore, automated tests and CI do not require a real Groq API key or a live network request.

## Manual AI Testing Procedure

The real Java-to-Groq integration was also manually verified during Alpha development.

### Setup

Set the API key locally:

```bash
export GROQ_API_KEY="your-api-key"
```

Do not place the real key in source code, documentation, commits, or test files.

### Verification

A real request was sent through the Java `GroqRecommendationService` to the Groq API.

The service successfully received, parsed, validated, and returned a break recommendation.

One observed result was:

```text
Step outside for a brisk 5-minute walk to stretch your legs and refresh your mind.
```

AI responses are nondeterministic, so manual testing should not require this exact sentence.

Instead, verify that the response:

- Is not empty.
- Contains a reasonable break recommendation.
- Is concise.
- Does not exceed the configured word limit.
- Does not expose model reasoning.

## Fallback Testing

The application must continue to provide a recommendation when the AI service cannot be used.

Fallback behavior can occur when:

- `GROQ_API_KEY` is missing.
- The provider returns an invalid response.
- Retryable failures continue after all attempts.
- Response validation fails.

`FallbackRecommendationService` provides simple rule-based recommendations based on the completed session count.

This prevents an AI provider failure from breaking the core recommendation feature.

## Technical Debt and Potential Resolutions

### 1. Direct Desktop-to-AI Communication

**Technical debt:**  
For the Alpha release, the Java desktop application communicates directly with the Groq API.

**Impact:**  
A distributed desktop application cannot securely protect a provider API key in a production environment.

**Potential resolution:**  
Move AI communication to a backend or proxy service. The backend would securely store the provider credential, while the JavaFX client would communicate only with the application's backend.

### 2. Fixed Retry Delay

**Technical debt:**  
The current implementation uses a fixed one-second delay between retry attempts.

**Impact:**  
A fixed delay may not be optimal during rate limiting or longer provider outages.

**Potential resolution:**  
Implement exponential backoff and support the provider's `Retry-After` information when available.

### 3. Limited Recommendation Inputs

**Technical debt:**  
The current recommendation request primarily uses completed session count.

**Impact:**  
The AI has limited information available for personalization.

**Potential resolution:**  
Expand `RecommendationRequest` to include relevant information such as break preferences, work-session duration, and recent break history as those modules become available.

### 4. Basic AI Response Validation

**Technical debt:**  
Current validation focuses mainly on response format and length.

**Impact:**  
It does not perform advanced semantic evaluation of every recommendation.

**Potential resolution:**  
Add stronger validation and testing for recommendation relevance, safety, and consistency.

## Alpha Status

The AI recommendation module currently provides:

- A modular `RecommendationService` interface.
- Groq AI integration.
- Asynchronous HTTP requests.
- Environment-variable API key handling.
- JSON response parsing.
- Response validation.
- Retry and error handling.
- Rule-based fallback recommendations.
- Automated unit tests.
- Manual live API verification.
- Integration with the JavaFX session and recommendation-dialog flow.

The recommendation service is connected to the application's session flow. Recommendation
requests are handled asynchronously and successful results are published through the existing
`DialogState` and `DialogDisplay` lifecycle. A recommendation displayed in the JavaFX interface
may come from Groq or from `FallbackRecommendationService`, depending on provider availability,
response validity, and retry outcomes.
