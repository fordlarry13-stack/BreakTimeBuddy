package com.breaktimebuddy;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import com.google.gson.annotations.Since;

/**
 * Data transfer object that carries the data saved to the configuration file. This class is
 * immutable and therefore thread‑safe if {@code history} is too. The result of
 * {@link #sanitize(ConfigData)} ensures this.
 *
 * The default values set in {@link #sanitize(ConfigData)} are also used as the initial values in
 * {@link Interactor}.
 *
 * When the data is loaded, always pass it through {@link #sanitize(ConfigData)}.
 */
public record ConfigData(@Since(1.0) int sessions, @Since(1.0) Duration preferredWorkLength,
    @Since(1.0) List<HistoryItem> history) {
  /** The record component that corresponds to {@link HistoryItem}. */
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

    /**
     * Validates and sanitizes the given data, correcting missing, invalid, or inconsistent data,
     * and logs any errors to the console. Missing fields are filled with the default values.
     * Invalid or inconsistent data is fixed whenever possible; unfixable errors are removed and
     * replaced with the default values. If the whole data is not fixable, then returns
     * {@code null}.
     *
     * This considers bad {@code beginTime} and {@code endTime} unfixable.
     *
     * @param item the data to be sanitized
     * @return a copy of the data after sanitization, or null if the item cannot be fixed
     */
    public static HistoryItem trySanitize(HistoryItem item) {
      return trySanitize(item, true);
    }
  }

  /**
   * Validates and sanitizes the given data, correcting missing, invalid, or inconsistent values. If
   * output is true, errors are logged to the console. The result is guaranteed to be immutable and
   * therefore thread‑safe.
   *
   * @param data the data to be sanitized; if null, the default data is returned
   * @param output if true, errors are logged to the console; if false, they are suppressed
   * @return a copy of the data after sanitization
   */
  private static ConfigData sanitize(ConfigData data, boolean output) {
    int sessions = 0;
    Duration preferredWorkLength = Duration.of(PreferencesHelper.DEFAULT_PREFERRED_WORK_LENGTH,
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

      preferredWorkLength = PreferencesHelper.defaultClampAndQuantize(data.preferredWorkLength,
          PreferencesHelper.DEFAULT_PREFERRED_WORK_LENGTH,
          PreferencesHelper.MIN_PREFERRED_WORK_LENGTH, PreferencesHelper.MAX_PREFERRED_WORK_LENGTH,
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

        history = data.history.stream().limit(HistoryItem.HISTORY_LENGTH)
            .map(item -> HistoryItem.trySanitize(item, output)).filter(item -> item != null)
            .toList(); // An unmodifiable list
      }
    }

    preferredWorkLength = PreferencesHelper.defaultClampAndQuantize(preferredWorkLength,
        PreferencesHelper.DEFAULT_PREFERRED_WORK_LENGTH,
        PreferencesHelper.MIN_PREFERRED_WORK_LENGTH, PreferencesHelper.MAX_PREFERRED_WORK_LENGTH,
        PreferencesHelper.UNIT_PREFERRED_WORK_LENGTH);

    return new ConfigData(sessions, preferredWorkLength, history);
  }

  /**
   * Validates and sanitizes the given data, correcting missing, invalid, or inconsistent data, and
   * logs any errors to the console. Missing fields are filled with the default values. Invalid or
   * inconsistent data is fixed whenever possible; unfixable errors are removed and replaced with
   * the default values. The result is guaranteed to be immutable and therefore thread‑safe.
   *
   * @param data the data to be sanitized; if null, the default data is returned
   * @return a copy of the data after sanitization
   */
  public static ConfigData sanitize(ConfigData data) {
    return sanitize(data, true);
  }

  /**
   * Returns a default {@code ConfigData} instance. It has the following parameters:
   *
   * <ul>
   * <li>{@code sessions = 0}
   * <li>Empty {@code history}
   * <li>Default {@code preferredWorkLength} defined with
   * {@link PreferencesHelper#DEFAULT_PREFERRED_WORK_LENGTH}
   * </ul>
   */
  public static ConfigData getDefault() {
    return sanitize(null, false);
  }
}
