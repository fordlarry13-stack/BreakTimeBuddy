package com.breaktimebuddy;

/**
 * Object containing the response to a break recommendation request. This class is immutable and
 * therefore thread‑safe.
 *
 * @param shouldBreak whether to recommend a break
 * @param activity if {@code shouldBreak} is true, the recommended break activity
 */
public record RecommendationResponse(boolean shouldBreak, String activity) {
}
