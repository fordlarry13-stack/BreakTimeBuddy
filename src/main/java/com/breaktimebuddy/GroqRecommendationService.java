package com.breaktimebuddy;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Implementation of {@link RecommendationService} that uses the Groq API to generate break
 * recommendations, with a fallback service for failures.
 *
 * The Groq API key is read from the environment variable {@code GROQ_API_KEY}.
 */
public class GroqRecommendationService implements RecommendationService {

  private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";

  private static final String MODEL = "qwen/qwen3.8-27b";

  private static final int MAX_ATTEMPTS = 3;
  private static final int RETRY_DELAY_SECONDS = 1;

  private final GroqHttpClient httpClient;
  private final RecommendationService fallbackService;
  private final String apiKey;

  /**
   * Creates a service that reads the Groq API key from the {@code GROQ_API_KEY} environment
   * variable.
   */
  public GroqRecommendationService() {
    this(new DefaultGroqHttpClient(), new FallbackRecommendationService(),
        System.getenv("GROQ_API_KEY"));
  }

  /**
   * Creates a service with the given dependencies for swapping implementations.
   *
   * @param httpClient the HTTP client for API requests
   * @param fallbackService the fallback service for failures
   * @param apiKey the Groq API key, or null to use fallback immediately
   */
  GroqRecommendationService(GroqHttpClient httpClient, RecommendationService fallbackService,
      String apiKey) {
    this.httpClient = httpClient;
    this.fallbackService = fallbackService;
    this.apiKey = apiKey;
  }

  @Override
  public CompletableFuture<RecommendationResponse> getRecommendation(
      RecommendationRequest request) {
    if (apiKey == null || apiKey.isBlank()) {
      return fallbackService.getRecommendation(request);
    }

    String prompt = buildPrompt(request);
    String requestBody = buildRequestBody(prompt);

    HttpRequest httpRequest =
        HttpRequest.newBuilder().uri(URI.create(API_URL)).timeout(Duration.ofSeconds(10))
            .header("Authorization", "Bearer " + apiKey).header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody)).build();

    return sendWithRetry(request, httpRequest, 1);
  }

  private CompletableFuture<RecommendationResponse> sendWithRetry(RecommendationRequest request,
      HttpRequest httpRequest, int attempt) {
    return httpClient.send(httpRequest).thenCompose(response -> {
      if (response.statusCode() == 200) {
        ResponseJsonObject recommendation = extractRecommendation(response.body());
        RecommendationResponse recommendationResponse =
            ResponseJsonObject.tryToRecommendationResponse(recommendation);

        if (recommendationResponse == null)
          return fallbackService.getRecommendation(request);
        if (recommendationResponse.shouldBreak()
            && !isValidRecommendation(recommendationResponse.activity()))
          return fallbackService.getRecommendation(request);
        return CompletableFuture.completedFuture(recommendationResponse);
      }

      if (isRetryableStatus(response.statusCode()) && attempt < MAX_ATTEMPTS) {
        return retryLater(request, httpRequest, attempt + 1);
      }

      return fallbackService.getRecommendation(request);
    }).exceptionallyCompose(error -> {

      if (attempt < MAX_ATTEMPTS) {
        return retryLater(request, httpRequest, attempt + 1);
      }

      return fallbackService.getRecommendation(request);
    });
  }

  private CompletableFuture<RecommendationResponse> retryLater(RecommendationRequest request,
      HttpRequest httpRequest, int nextAttempt) {
    return CompletableFuture.runAsync(() -> {
    }, CompletableFuture.delayedExecutor(RETRY_DELAY_SECONDS, TimeUnit.SECONDS))
        .thenCompose(ignored -> sendWithRetry(request, httpRequest, nextAttempt));
  }

  private boolean isRetryableStatus(int statusCode) {
    return statusCode == 429 || statusCode >= 500;
  }

  private String formatDuration(Duration duration) {
    return "%d:%02d:%02d".formatted(duration.toHours(), duration.toMinutesPart(),
        duration.toSecondsPart());
  }

  private String buildPrompt(RecommendationRequest request) {
    String workHistorySection = request.history().size() == 0
        ? "The user has not completed any sessions."
        : """
            The following are the user's recent complete work and break sessions, \
            in order from recent to oldest:
            %s
            """.formatted(
            request.history().stream().map(item -> "- %s for %s".formatted(switch (item.phase()) {
              case WORK -> "Work";
              case BREAK -> "Break";
            }, formatDuration(Duration.between(item.beginTime(), item.endTime()))))
                .collect(Collectors.joining("\n")));
    return """
        You are Break Time Buddy.

        The user has completed %d work sessions.

        Their preferred work session length is %s.

        The user has been working continuously for %s.

        %s

        Decide whether the user should take a break now.

        Only if the user should take a break, recommend one short, healthy break activity.
        Keep the response under 30 words.
        Do not include medical advice.
        Return only the recommendation.
        """.formatted(request.sessions(), formatDuration(request.preferredWorkLength()),
        formatDuration(request.workingDuration()), workHistorySection);
  }

  private record ResponseJsonObject(boolean shouldBreak, String activity) {
    static RecommendationResponse tryToRecommendationResponse(ResponseJsonObject o) {
      if (o.shouldBreak() && o.activity() == null)
        return null;
      return new RecommendationResponse(o.shouldBreak(),
          o.shouldBreak() ? o.activity().trim() : "");
    }
  }

  private String buildRequestBody(String prompt) {
    String escapedPrompt = escapeJson(prompt);
    // Strict Mode is not available with the model used
    return """
        {
          "model": "%s",
          "reasoning_format": "hidden",
          "messages": [
            {
              "role": "user",
              "content": "%s"
            }
          ],
          "response_format": {
            "type": "json_schema",
            "json_schema": {
              "name": "breakRecommendation",
              "strict": false,
              "schema": {
                "type": "object",
                "properties": {
                  "shouldBreak": {
                    "type": "boolean",
                    "description": "Whether the user should take a break"
                  },
                  "activity": {
                    "type": "string",
                    "description": "A recommended break activity"
                  }
                },
                "required": ["shouldBreak"],
                "additionalProperties": false
              }
            }
          }
        }
        """.formatted(MODEL, escapedPrompt);
  }

  private ResponseJsonObject extractRecommendation(String responseBody) {
    try {
      JsonObject root = JsonParser.parseString(responseBody).getAsJsonObject();

      JsonArray choices = root.getAsJsonArray("choices");

      if (choices == null || choices.isEmpty()) {
        return null;
      }

      JsonObject message = choices.get(0).getAsJsonObject().getAsJsonObject("message");

      if (message == null || !message.has("content") || message.get("content").isJsonNull()) {
        return null;
      }

      String contentString = message.get("content").getAsString().trim();
      // JsonSyntaxException is RuntimeException
      return new Gson().fromJson(contentString, ResponseJsonObject.class);
    } catch (RuntimeException error) {
      return null;
    }
  }

  private boolean isValidRecommendation(String recommendation) {
    if (recommendation == null || recommendation.isBlank()) {
      return false;
    }

    String normalized = recommendation.trim();
    String lowerCase = normalized.toLowerCase(Locale.ROOT);

    if (lowerCase.contains("<think>") || lowerCase.contains("</think>")) {
      return false;
    }

    if (containsMedicalAdvice(lowerCase)) {
      return false;
    }

    int wordCount = normalized.split("\\s+").length;

    return wordCount <= 30;
  }

  private boolean containsMedicalAdvice(String lowerCase) {
    List<String> forbiddenList = List.of("ibuprofen", "aspirin", "acetaminophen", "medication",
        "medicine", "dosage", "dose", "diagnose", "diagnosis", "treatment", "prescription",
        "prescribe", "take a pain reliever", "take pain reliever", "stop taking", "start taking");
    return forbiddenList.stream().anyMatch(e -> lowerCase.contains(e));
  }

  private String escapeJson(String value) {
    return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
  }
}
