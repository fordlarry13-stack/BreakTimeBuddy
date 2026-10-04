package com.breaktimebuddy;

import java.time.Duration;
import java.util.List;

/**
 * Request object containing context for a break recommendation. This class is immutable and
 * therefore thread‑safe if {@code history} is too.
 *
 * @param sessions the number of completed sessions
 * @param preferredWorkLength the preferred work length
 * @param workingDuration the duration of the current work session
 * @param history the records of recent completed sessions
 */
public record RecommendationRequest(int sessions, Duration preferredWorkLength,
    Duration workingDuration, List<HistoryItem> history) {
}
