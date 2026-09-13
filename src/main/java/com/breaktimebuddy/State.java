package com.breaktimebuddy;

/** Public inner state snapshot */
public record State(boolean inSession, int sessions) {
}
