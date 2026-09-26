package com.breaktimebuddy;

import java.util.List;

/** Public inner state snapshot */
public record State(boolean inSession, int sessions, List<HistoryItem> history,
                boolean breakRecommendationRequested, DialogState dialogState) {
}
