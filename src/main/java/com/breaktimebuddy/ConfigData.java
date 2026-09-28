package com.breaktimebuddy;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import com.google.gson.annotations.Since;

public record ConfigData(@Since(1.0) int sessions,
    @Since(1.0) Duration preferredWorkLength,
    @Since(1.0) List<HistoryItem> history) {

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
          System.out.println("ConfigData.HistoryItem.trySanitize(): item.beginTimeis null");
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
    Duration preferredWorkLength = Duration.of(
        PreferencesHelper.DEFAULT_PREFERRED_WORK_LENGTH,
        PreferencesHelper.UNIT_PREFERRED_WORK_LENGTH);
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

      preferredWorkLength = PreferencesHelper.defaultClampAndQuantize(
          data.preferredWorkLength,
          PreferencesHelper.DEFAULT_PREFERRED_WORK_LENGTH,
          PreferencesHelper.MIN_PREFERRED_WORK_LENGTH,
          PreferencesHelper.MAX_PREFERRED_WORK_LENGTH,
          PreferencesHelper.UNIT_PREFERRED_WORK_LENGTH);

      if (!Objects.equals(preferredWorkLength, data.preferredWorkLength)) {
        if (output)
          System.out.println(
              "ConfigData.sanitize(): data.preferredWorkLength is null or not clamped and quantized");
      }

      if (data.history == null) {
        if (output)
          System.out.println("ConfigData.sanitize(): data.history is null");
      } else {
        if (data.history.size() > HistoryItem.HISTORY_LENGTH)
          if (output)
            System.out.println("ConfigData.sanitize(): data.history is too large");

        history = data.history.stream()
            .limit(HistoryItem.HISTORY_LENGTH)
            .map(item -> HistoryItem.trySanitize(item, output))
            .filter(item -> item != null)
            .toList();
      }
    }

    preferredWorkLength = PreferencesHelper.defaultClampAndQuantize(
        preferredWorkLength,
        PreferencesHelper.DEFAULT_PREFERRED_WORK_LENGTH,
        PreferencesHelper.MIN_PREFERRED_WORK_LENGTH,
        PreferencesHelper.MAX_PREFERRED_WORK_LENGTH,
        PreferencesHelper.UNIT_PREFERRED_WORK_LENGTH);

    return new ConfigData(sessions, preferredWorkLength, history);
  }

  public static ConfigData sanitize(ConfigData data) {
    return sanitize(data, true);
  }

  public static ConfigData getDefault() {
    return sanitize(null, false);
  }
}
