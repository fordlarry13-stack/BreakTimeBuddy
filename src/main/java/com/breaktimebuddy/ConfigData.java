package com.breaktimebuddy;

import java.time.Instant;
import java.util.List;
import com.google.gson.annotations.Since;

public record ConfigData(@Since(1.0) int sessions, @Since(1.0) List<HistoryItem> history) {
  public record HistoryItem(@Since(1.0) Phase phase, @Since(1.0) Instant beginTime,
      @Since(1.0) Instant endTime) {
    public enum Phase {
      WORK, BREAK;
    }
  }
}
