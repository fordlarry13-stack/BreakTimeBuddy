package com.breaktimebuddy;

import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLSession;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GroqRecommendationServiceTest {

    @Test
    void returnsAiRecommendationWhenResponseIsValid() {
        GroqHttpClient client = request ->
                CompletableFuture.completedFuture(
                        response(
                                200,
                                """
                                {
                                  "choices": [
                                    {
                                      "message": {
                                        "content": "Stand up and stretch for five minutes."
                                      }
                                    }
                                  ]
                                }
                                """
                        )
                );

        GroqRecommendationService service =
                new GroqRecommendationService(
                        client,
                        new FallbackRecommendationService(),
                        "test-api-key"
                );

        String result = service
                .getRecommendation(new RecommendationRequest(3))
                .join();

        assertEquals(
                "Stand up and stretch for five minutes.",
                result
        );
    }

    @Test
    void usesFallbackWhenResponseJsonIsInvalid() {
        GroqHttpClient client = request ->
                CompletableFuture.completedFuture(
                        response(200, "not valid json")
                );

        GroqRecommendationService service =
                new GroqRecommendationService(
                        client,
                        new FallbackRecommendationService(),
                        "test-api-key"
                );

        String result = service
                .getRecommendation(new RecommendationRequest(3))
                .join();

        assertEquals(
                "Take a 5-minute break and stretch.",
                result
        );
    }

    @Test
    void usesFallbackWhenRecommendationIsTooLong() {
        String longRecommendation = "word ".repeat(31).trim();

        String body = """
                {
                  "choices": [
                    {
                      "message": {
                        "content": "%s"
                      }
                    }
                  ]
                }
                """.formatted(longRecommendation);

        GroqHttpClient client = request ->
                CompletableFuture.completedFuture(
                        response(200, body)
                );

        GroqRecommendationService service =
                new GroqRecommendationService(
                        client,
                        new FallbackRecommendationService(),
                        "test-api-key"
                );

        String result = service
                .getRecommendation(new RecommendationRequest(4))
                .join();

        assertEquals(
                "Take a 10-minute break. Walk around, stretch, and drink some water.",
                result
        );
    }

    @Test
    void usesFallbackWhenApiKeyIsMissing() {
        GroqHttpClient client = request -> {
            throw new AssertionError(
                    "HTTP client should not be called without an API key"
            );
        };

        GroqRecommendationService service =
                new GroqRecommendationService(
                        client,
                        new FallbackRecommendationService(),
                        ""
                );

        String result = service
                .getRecommendation(new RecommendationRequest(1))
                .join();

        assertEquals(
                "Take a short break and rest your eyes.",
                result
        );
    }


    @Test
    void retriesAfter429AndThenReturnsAiRecommendation() {
        java.util.concurrent.atomic.AtomicInteger calls =
                new java.util.concurrent.atomic.AtomicInteger();

        GroqHttpClient client = request -> {
            int call = calls.incrementAndGet();

            if (call == 1) {
                return CompletableFuture.completedFuture(
                        response(429, "{}")
                );
            }

            return CompletableFuture.completedFuture(
                    response(
                            200,
                            """
                            {
                              "choices": [
                                {
                                  "message": {
                                    "content": "Take a short walk and stretch."
                                  }
                                }
                              ]
                            }
                            """
                    )
            );
        };

        GroqRecommendationService service =
                new GroqRecommendationService(
                        client,
                        new FallbackRecommendationService(),
                        "test-api-key"
                );

        String result = service
                .getRecommendation(new RecommendationRequest(2))
                .join();

        assertEquals(
                "Take a short walk and stretch.",
                result
        );

        assertEquals(2, calls.get());
    }

    @Test
    void retriesAfter500AndThenReturnsAiRecommendation() {
        java.util.concurrent.atomic.AtomicInteger calls =
                new java.util.concurrent.atomic.AtomicInteger();

        GroqHttpClient client = request -> {
            int call = calls.incrementAndGet();

            if (call == 1) {
                return CompletableFuture.completedFuture(
                        response(500, "{}")
                );
            }

            return CompletableFuture.completedFuture(
                    response(
                            200,
                            """
                            {
                              "choices": [
                                {
                                  "message": {
                                    "content": "Rest your eyes and take a brief stretch."
                                  }
                                }
                              ]
                            }
                            """
                    )
            );
        };

        GroqRecommendationService service =
                new GroqRecommendationService(
                        client,
                        new FallbackRecommendationService(),
                        "test-api-key"
                );

        String result = service
                .getRecommendation(new RecommendationRequest(2))
                .join();

        assertEquals(
                "Rest your eyes and take a brief stretch.",
                result
        );

        assertEquals(2, calls.get());
    }

    @Test
    void retriesAfterNetworkFailureAndThenSucceeds() {
        java.util.concurrent.atomic.AtomicInteger calls =
                new java.util.concurrent.atomic.AtomicInteger();

        GroqHttpClient client = request -> {
            int call = calls.incrementAndGet();

            if (call == 1) {
                return CompletableFuture.failedFuture(
                        new RuntimeException("Simulated network failure")
                );
            }

            return CompletableFuture.completedFuture(
                    response(
                            200,
                            """
                            {
                              "choices": [
                                {
                                  "message": {
                                    "content": "Stand up, breathe, and stretch for a few minutes."
                                  }
                                }
                              ]
                            }
                            """
                    )
            );
        };

        GroqRecommendationService service =
                new GroqRecommendationService(
                        client,
                        new FallbackRecommendationService(),
                        "test-api-key"
                );

        String result = service
                .getRecommendation(new RecommendationRequest(3))
                .join();

        assertEquals(
                "Stand up, breathe, and stretch for a few minutes.",
                result
        );

        assertEquals(2, calls.get());
    }

    @Test
    void doesNotRetry401AndUsesFallbackImmediately() {
        java.util.concurrent.atomic.AtomicInteger calls =
                new java.util.concurrent.atomic.AtomicInteger();

        GroqHttpClient client = request -> {
            calls.incrementAndGet();

            return CompletableFuture.completedFuture(
                    response(401, "{}")
            );
        };

        GroqRecommendationService service =
                new GroqRecommendationService(
                        client,
                        new FallbackRecommendationService(),
                        "test-api-key"
                );

        String result = service
                .getRecommendation(new RecommendationRequest(3))
                .join();

        assertEquals(
                "Take a 5-minute break and stretch.",
                result
        );

        assertEquals(1, calls.get());
    }

    private static HttpResponse<String> response(
            int statusCode,
            String body
    ) {
        return new HttpResponse<>() {

            @Override
            public int statusCode() {
                return statusCode;
            }

            @Override
            public HttpRequest request() {
                return null;
            }

            @Override
            public Optional<HttpResponse<String>> previousResponse() {
                return Optional.empty();
            }

            @Override
            public HttpHeaders headers() {
                return HttpHeaders.of(
                        Map.of(),
                        (name, value) -> true
                );
            }

            @Override
            public String body() {
                return body;
            }

            @Override
            public Optional<SSLSession> sslSession() {
                return Optional.empty();
            }

            @Override
            public URI uri() {
                return URI.create(
                        "https://api.groq.com/openai/v1/chat/completions"
                );
            }

            @Override
            public HttpClient.Version version() {
                return HttpClient.Version.HTTP_1_1;
            }
        };
    }
}
