package com.breaktimebuddy;

import java.time.Duration;
import java.util.List;

/**
 * Request object containing context for a break recommendation.
 *
 * @param inSession whether a work session is active
 * @param sessions the number of completed sessions
 * @param history the records of recent completed sessions
 */
public record RecommendationRequest(int sessions, Duration preferredWorkLength,
    Duration workingDuration, List<HistoryItem> history) {
}
