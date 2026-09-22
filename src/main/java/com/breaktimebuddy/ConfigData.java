package com.breaktimebuddy;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import com.google.gson.annotations.Since;

public record ConfigData(@Since(1.0) int sessions, @Since(1.0) Duration preferredWorkLength) {
  private static ConfigData sanitize(ConfigData data, boolean output) {
    int sessions = 0;
    Duration preferredWorkLength = Duration.of(50, ChronoUnit.MINUTES);
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
      if (data.preferredWorkLength == null) {
        if (output)
          System.out.println("ConfigData.sanitize(): data.preferredWorkLength is null");
      } else {
        preferredWorkLength = PreferencesHelper.clampAndQuantize(data.preferredWorkLength,
            PreferencesHelper.MIN_PREFERRED_WORK_LENGTH,
            PreferencesHelper.MAX_PREFERRED_WORK_LENGTH,
            PreferencesHelper.UNIT_PREFERRED_WORK_LENGTH);
        if (!preferredWorkLength.equals(data.preferredWorkLength)) {
          if (output)
            System.out.println(
                "ConfigData.sanitize(): data.preferredWorkLength is not clamped and quantized");
        }
      }
    }
    preferredWorkLength = PreferencesHelper.clampAndQuantize(preferredWorkLength,
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
