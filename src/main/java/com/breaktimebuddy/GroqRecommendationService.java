package com.breaktimebuddy;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class GroqRecommendationService implements RecommendationService {

  private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";

  private static final String MODEL = "qwen/qwen3.8-27b";

  private static final int MAX_ATTEMPTS = 3;
  private static final int RETRY_DELAY_SECONDS = 1;

  private final GroqHttpClient httpClient;
  private final RecommendationService fallbackService;
  private final String apiKey;

  public GroqRecommendationService() {
    this(new DefaultGroqHttpClient(), new FallbackRecommendationService(),
        System.getenv("GROQ_API_KEY"));
  }

  GroqRecommendationService(GroqHttpClient httpClient, RecommendationService fallbackService,
      String apiKey) {
    this.httpClient = httpClient;
    this.fallbackService = fallbackService;
    this.apiKey = apiKey;
  }

  @Override
  public CompletableFuture<String> getRecommendation(RecommendationRequest request) {
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

  private CompletableFuture<String> sendWithRetry(RecommendationRequest request,
      HttpRequest httpRequest, int attempt) {
    return httpClient.send(httpRequest).thenCompose(response -> {

      if (response.statusCode() == 200) {
        String recommendation = extractRecommendation(response.body());

        if (!isValidRecommendation(recommendation)) {
          return fallbackService.getRecommendation(request);
        }

        return CompletableFuture.completedFuture(recommendation);
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

  private CompletableFuture<String> retryLater(RecommendationRequest request,
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
    return """
        You are Break Time Buddy.

        The user has completed %d work sessions. Their preferred work session length is %s.

        Recommend one short, healthy break activity.
        Keep the response under 30 words.
        Do not include medical advice.
        Return only the recommendation.
        """.formatted(request.sessions(), formatDuration(request.preferredWorkLength()));
  }

  private String buildRequestBody(String prompt) {
    String escapedPrompt = escapeJson(prompt);

    return """
        {
          "model": "%s",
          "reasoning_format": "hidden",
          "messages": [
            {
              "role": "user",
              "content": "%s"
            }
          ]
        }
        """.formatted(MODEL, escapedPrompt);
  }

  private String extractRecommendation(String responseBody) {
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

      return message.get("content").getAsString().trim();

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

    int wordCount = normalized.split("\\s+").length;

    return wordCount <= 30;
  }

  private String escapeJson(String value) {
    return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
  }
}
