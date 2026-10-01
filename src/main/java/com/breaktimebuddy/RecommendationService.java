package com.breaktimebuddy;

import java.util.concurrent.CompletableFuture;

public interface RecommendationService {
  CompletableFuture<RecommendationResponse> getRecommendation(RecommendationRequest request);
}
