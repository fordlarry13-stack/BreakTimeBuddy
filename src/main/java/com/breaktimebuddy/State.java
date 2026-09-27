package com.breaktimebuddy;

/**
 * Public inner state snapshot.
 *
 * <ul>
 * <li>{@code inSession} - whether a work session is active
 * <li>{@code sessions} - the number of completed sessions
 * <li>{@code breakRecommendationRequested} - whether a break recommendation is pending
 * <li>{@code dialogState} - the current dialog state, or null if none
 * </ul>
 */
public record State(boolean inSession, int sessions, boolean breakRecommendationRequested,
    DialogState dialogState) {
}
