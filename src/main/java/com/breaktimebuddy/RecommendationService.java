package com.breaktimebuddy;

import java.util.concurrent.CompletableFuture;

public interface RecommendationService {

    CompletableFuture<String> getRecommendation(RecommendationRequest request);
}
