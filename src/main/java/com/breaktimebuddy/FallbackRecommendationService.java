package com.breaktimebuddy;

import java.util.concurrent.CompletableFuture;

public class FallbackRecommendationService implements RecommendationService {

    @Override
    public CompletableFuture<String> getRecommendation(RecommendationRequest request) {

        String recommendation;

        if (request.sessions() >= 4) {
            recommendation = "Take a 10-minute break. Walk around, stretch, and drink some water.";
        } else if (request.sessions() >= 2) {
            recommendation = "Take a 5-minute break and stretch.";
        } else {
            recommendation = "Take a short break and rest your eyes.";
        }

        return CompletableFuture.completedFuture(recommendation);
    }
}
