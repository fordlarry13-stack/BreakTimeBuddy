package com.breaktimebuddy;

import java.time.Duration;
import java.util.Objects;
import com.google.gson.annotations.Since;

public record ConfigData(@Since(1.0) int sessions, @Since(1.0) Duration preferredWorkLength) {
  private static ConfigData sanitize(ConfigData data, boolean output) {
    int sessions = 0;
    Duration preferredWorkLength = Duration.of(PreferencesHelper.DEFAULT_PREFERRED_WORK_LENGTH,
        PreferencesHelper.UNIT_PREFERRED_WORK_LENGTH);
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
      preferredWorkLength = PreferencesHelper.defaultClampAndQuantize(data.preferredWorkLength,
          PreferencesHelper.DEFAULT_PREFERRED_WORK_LENGTH,
          PreferencesHelper.MIN_PREFERRED_WORK_LENGTH, PreferencesHelper.MAX_PREFERRED_WORK_LENGTH,
          PreferencesHelper.UNIT_PREFERRED_WORK_LENGTH);
      if (!Objects.equals(preferredWorkLength, data.preferredWorkLength)) {
        if (output)
          System.out.println(
              "ConfigData.sanitize(): data.preferredWorkLength is null or not clamped and quantized");
      }
    }
    preferredWorkLength = PreferencesHelper.defaultClampAndQuantize(preferredWorkLength,
        PreferencesHelper.DEFAULT_PREFERRED_WORK_LENGTH,
        PreferencesHelper.MIN_PREFERRED_WORK_LENGTH, PreferencesHelper.MAX_PREFERRED_WORK_LENGTH,
        PreferencesHelper.UNIT_PREFERRED_WORK_LENGTH);
    return new ConfigData(sessions, preferredWorkLength);
  }

  public static ConfigData sanitize(ConfigData data) {
    return sanitize(data, true);
  }

  public static ConfigData getDefault() {
    return sanitize(null, false);
  }
}
