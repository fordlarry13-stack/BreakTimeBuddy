package com.breaktimebuddy;

import static org.junit.jupiter.api.Assertions.*;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

public class FallbackRecommendationServiceTest {

  @Test
  void returnsShortBreakForOneSession() {
    RecommendationService service = new FallbackRecommendationService();

    RecommendationResponse result = service
        .getRecommendation(
            new RecommendationRequest(1, Duration.ofMinutes(10), Duration.ofMinutes(10), List.of()))
        .join();
    assertTrue(result.shouldBreak());
    assertEquals("Take a short break and rest your eyes.", result.activity());
  }

  @Test
  void returnsFiveMinuteBreakForThreeSessions() {
    RecommendationService service = new FallbackRecommendationService();

    RecommendationResponse result = service
        .getRecommendation(
            new RecommendationRequest(3, Duration.ofMinutes(10), Duration.ofMinutes(10), List.of()))
        .join();

    assertTrue(result.shouldBreak());
    assertEquals("Take a 5-minute break and stretch.", result.activity());
  }

  @Test
  void returnsTenMinuteBreakForFourSessions() {
    RecommendationService service = new FallbackRecommendationService();

    RecommendationResponse result = service
        .getRecommendation(
            new RecommendationRequest(4, Duration.ofMinutes(10), Duration.ofMinutes(10), List.of()))
        .join();

    assertTrue(result.shouldBreak());
    assertEquals("Take a 10-minute break. Walk around, stretch, and drink some water.",
        result.activity());
  }

  @Test
  void returnsNoBreakShortlyAfterStart() {
    RecommendationService service = new FallbackRecommendationService();

    RecommendationResponse result = service
        .getRecommendation(
            new RecommendationRequest(4, Duration.ofMinutes(10), Duration.ofMinutes(1), List.of()))
        .join();

    assertFalse(result.shouldBreak());
  }
}
