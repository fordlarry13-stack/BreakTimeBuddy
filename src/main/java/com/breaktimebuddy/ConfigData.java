package com.breaktimebuddy;

import com.google.gson.annotations.Since;

public record ConfigData(@Since(1.0) int sessions) {
  public static ConfigData withDefaults(ConfigData data) {
    int sessions = 0;
    if (data == null) {
      System.out.println("ConfigData.withDefaults(): data is null");
    } else {
      sessions = data.sessions();
    }
    return new ConfigData(sessions);
  }
}
