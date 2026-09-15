package com.breaktimebuddy;

import com.google.gson.annotations.Since;

public record ConfigData(@Since(1.0) int sessions) {
  private static ConfigData sanitize(ConfigData data, boolean output) {
    int sessions = 0;
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
    }
    return new ConfigData(sessions);
  }

  public static ConfigData sanitize(ConfigData data) {
    return sanitize(data, true);
  }

  public static ConfigData getDefault() {
    return sanitize(null, false);
  }
}
