package com.breaktimebuddy;

import java.time.Duration;

public record RecommendationRequest(int sessions, Duration preferredWorkLength) {
}
