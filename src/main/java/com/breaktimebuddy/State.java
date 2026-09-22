package com.breaktimebuddy;

import java.time.Duration;

/** Public inner state snapshot */
public record State(boolean inSession, int sessions, Duration preferredWorkLength,
                boolean breakRecommendationRequested, DialogState dialogState) {
}
