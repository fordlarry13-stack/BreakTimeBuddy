package com.breaktimebuddy;

import java.time.Duration;
import java.util.List;

/**
 * Public inner state snapshot.
 *
 * @param inSession whether a work session is active
 * @param sessions the number of completed sessions
 * @param history the records of recent completed sessions
 * @param breakRecommendationRequested whether a break recommendation is pending
 * @param dialogState the current dialog state, or null if none
 */
public record State(boolean inSession, int sessions, Duration preferredWorkLength,
    List<HistoryItem> history, boolean breakRecommendationRequested, DialogState dialogState) {
}
