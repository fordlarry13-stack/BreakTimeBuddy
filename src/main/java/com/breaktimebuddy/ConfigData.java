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

    private static final int HISTORY_LENGTH = 20;

    private static HistoryItem trySanitize(HistoryItem item, boolean output) {
      if (item == null) {
        if (output)
          System.out.println("ConfigData.HistoryItem.trySanitize(): item is null");
        return null;
      }
      if (item.beginTime == null) {
        if (output)
          System.out.println("ConfigData.HistoryItem.trySanitize(): item.beginTime is null");
        return null;
      }
      if (item.endTime == null) {
        if (output)
          System.out.println("ConfigData.HistoryItem.trySanitize(): item.endTime is null");
        return null;
      }
      if (!item.beginTime.isBefore(item.endTime)) {
        if (output)
          System.out.println(
              "ConfigData.HistoryItem.trySanitize(): item.beginTime is not before item.endTime");
        return null;
      }
      Phase phase = Phase.WORK;
      if (item.phase == null) {
        if (output)
          System.out.println("ConfigData.HistoryItem.trySanitize(): item.phase is null");
      } else {
        phase = item.phase;
      }
      return new HistoryItem(phase, item.beginTime, item.endTime);
    }

    public static HistoryItem trySanitize(HistoryItem item) {
      return trySanitize(item, true);
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
        sessions = data.sessions;
      }
      if (data.history == null) {
        if (output)
          System.out.println("ConfigData.sanitize(): data.history is null");
      } else {
        if (data.history.size() > HistoryItem.HISTORY_LENGTH)
          if (output)
            System.out.println("ConfigData.sanitize(): data.history is too large");
        history = data.history.stream().limit(HistoryItem.HISTORY_LENGTH)
            .map(item -> HistoryItem.trySanitize(item, output)).filter(item -> item != null)
            .toList();
      }
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
