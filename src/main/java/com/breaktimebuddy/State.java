package com.breaktimebuddy;

import java.time.Duration;
import java.util.List;

/** Public inner state snapshot */
public record State(boolean inSession, int sessions, Duration preferredWorkLength,
    List<HistoryItem> history, boolean breakRecommendationRequested, DialogState dialogState) {
}
