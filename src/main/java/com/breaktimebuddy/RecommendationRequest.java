package com.breaktimebuddy;

import java.time.Duration;
import java.util.List;

public record RecommendationRequest(int sessions, Duration workingDuration,
    List<HistoryItem> history) {
}
