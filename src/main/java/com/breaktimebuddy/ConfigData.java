package com.breaktimebuddy;

import com.google.gson.annotations.Since;

/**
 * Data transfer object that carries the number of completed sessions saved to the configuration
 * file. This class is immutable and therefore thread‑safe.
 *
 * The default values set in {@link #sanitize(ConfigData)} are also used as the initial values in
 * {@link Interactor}.
 *
 * When the data is loaded, always pass it through {@link #sanitize(ConfigData)}.
 */
public record ConfigData(@Since(1.0) int sessions) {
  /**
   * Validates and sanitizes the given data, correcting missing, invalid, or inconsistent values. If
   * output is true, errors are logged to the console.
   *
   * @param data the data to be sanitized; if null, the default data is returned
   * @param output if true, errors are logged to the console; if false, they are suppressed
   * @return a copy of the data after sanitization
   */
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

  /**
   * Validates and sanitizes the given data, correcting missing, invalid, or inconsistent data, and
   * logs any errors to the console. Missing fields are filled with the default values. Invalid or
   * inconsistent data is fixed whenever possible; unfixable errors are removed and replaced with
   * the default values.
   *
   * @param data the data to be sanitized; if null, the default data is returned
   * @return a copy of the data after sanitization
   */
  public static ConfigData sanitize(ConfigData data) {
    return sanitize(data, true);
  }

  /**
   * Returns a default {@code ConfigData} instance.
   */
  public static ConfigData getDefault() {
    return sanitize(null, false);
  }
}
