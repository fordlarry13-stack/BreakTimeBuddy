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

  private static ConfigData sanitize(ConfigData data, boolean output) {
    int sessions = 0;
    List<HistoryItem> history = List.of();
    if (data == null) {
      if (output)
        System.out.println("ConfigData.sanitize(): data is null");
    } else {
      if (data.sessions < 0) {
        if (output)
          System.out.println("ConfigData.sanitize(): data.sessions is invalid (negative)");
      } else {
        sessions = data.sessions();
      }
      // TODO
      history = data.history;
    }
    return new ConfigData(sessions, history);
  }

  public static ConfigData sanitize(ConfigData data) {
    return sanitize(data, true);
  }

  public static ConfigData getDefault() {
    return sanitize(null, false);
  }
}
