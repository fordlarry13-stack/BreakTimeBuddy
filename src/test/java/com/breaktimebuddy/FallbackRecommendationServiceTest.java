package com.breaktimebuddy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class FallbackRecommendationServiceTest {

    @Test
    void returnsShortBreakForOneSession() {
        RecommendationService service = new FallbackRecommendationService();

        String result = service.getRecommendation(new RecommendationRequest(1)).join();

        assertEquals("Take a short break and rest your eyes.", result);
    }

    @Test
    void returnsFiveMinuteBreakForThreeSessions() {
        RecommendationService service = new FallbackRecommendationService();

        String result = service.getRecommendation(new RecommendationRequest(3)).join();

        assertEquals("Take a 5-minute break and stretch.", result);
    }

    @Test
    void returnsTenMinuteBreakForFourSessions() {
        RecommendationService service = new FallbackRecommendationService();

        String result = service.getRecommendation(new RecommendationRequest(4)).join();

        assertEquals("Take a 10-minute break. Walk around, stretch, and drink some water.", result);
    }
}
