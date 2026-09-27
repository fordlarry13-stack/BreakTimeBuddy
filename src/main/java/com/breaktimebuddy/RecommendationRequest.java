package com.breaktimebuddy;

import java.time.Duration;
import java.util.List;

/** Request object containing context for a break recommendation. */
public record RecommendationRequest(int sessions, Duration workingDuration,
        List<HistoryItem> history) {
}
