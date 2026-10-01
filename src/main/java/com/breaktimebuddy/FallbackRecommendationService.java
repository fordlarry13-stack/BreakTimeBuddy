package com.breaktimebuddy;

import java.util.concurrent.CompletableFuture;

public class FallbackRecommendationService implements RecommendationService {
  @Override
  public CompletableFuture<RecommendationResponse> getRecommendation(
      RecommendationRequest request) {
    boolean shouldBreak = request.workingDuration()
        .compareTo(request.preferredWorkLength().multipliedBy(4).dividedBy(5)) > 0;

    String recommendation = "";
    if (shouldBreak) {
      if (request.sessions() >= 4) {
        recommendation = "Take a 10-minute break. Walk around, stretch, and drink some water.";
      } else if (request.sessions() >= 2) {
        recommendation = "Take a 5-minute break and stretch.";
      } else {
        recommendation = "Take a short break and rest your eyes.";
      }
    }

    return CompletableFuture
        .completedFuture(new RecommendationResponse(shouldBreak, recommendation));
  }
}
