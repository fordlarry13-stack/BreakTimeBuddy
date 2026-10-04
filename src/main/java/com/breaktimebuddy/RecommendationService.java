package com.breaktimebuddy;

import java.util.concurrent.CompletableFuture;

/**
 * Service interface for retrieving break recommendations asynchronously.
 */
public interface RecommendationService {
  /**
   * Returns a recommendation for a break activity based on the request.
   *
   * @param request the request containing session context
   * @return a future that completes with the recommendation response
   */
  CompletableFuture<RecommendationResponse> getRecommendation(RecommendationRequest request);
}
