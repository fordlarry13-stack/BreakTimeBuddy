package com.breaktimebuddy;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

/**
 * Utility class to handle preference data.
 */
public class PreferencesHelper {
  /** The default {@code preferredWorkLength} in units of {@link #UNIT_PREFERRED_WORK_LENGTH}. */
  public static final long DEFAULT_PREFERRED_WORK_LENGTH = 50;
  /** The minimum {@code preferredWorkLength} in units of {@link #UNIT_PREFERRED_WORK_LENGTH}. */
  public static final long MIN_PREFERRED_WORK_LENGTH = 1;
  /** The maximum {@code preferredWorkLength} in units of {@link #UNIT_PREFERRED_WORK_LENGTH}. */
  public static final long MAX_PREFERRED_WORK_LENGTH = 300;
  // public static final long MIN_PREFERRED_BREAK_LENGTH = 1;
  // public static final long MAX_PREFERRED_BREAK_LENGTH = 90;
  /**
   * The base unit for {@code preferredWorkLength}, {@link #DEFAULT_PREFERRED_WORK_LENGTH},
   * {@link #MIN_PREFERRED_WORK_LENGTH}, and {@link #MAX_PREFERRED_WORK_LENGTH}. Values are
   * truncated to multiples of this.
   */
  public static final TemporalUnit UNIT_PREFERRED_WORK_LENGTH = ChronoUnit.MINUTES;

  /**
   * Cleans a duration using the default, minimum, and maximum values, and a base unit. Replaces
   * {@code null} with the default value, clamps it within the ranges, and truncates it to a
   * multiple of the unit.
   *
   * @param duration the value to be cleaned; if null, the default value is returned
   * @param default_ the default value that replaces null in units of {@code unit}
   * @param min the minimum value in units of {@code unit}
   * @param max the maximum value in units of {@code unit}
   * @param unit the base unit for {@code default_}, {@code min}, and {@code max}
   * @return the cleaned value or clamping and quantization, or the default value if the input is
   *         null
   */
  public static Duration defaultClampAndQuantize(Duration duration, long default_, long min,
      long max, TemporalUnit unit) {
    if (duration == null)
      duration = Duration.of(default_, unit);
    duration = duration.truncatedTo(unit);
    if (duration.compareTo(Duration.of(min, unit)) < 0)
      duration = Duration.of(min, unit);
    if (duration.compareTo(Duration.of(max, unit)) > 0)
      duration = Duration.of(max, unit);
    return duration;
  }
}
