package com.breaktimebuddy;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

public class PreferencesHelper {
  public static final long MIN_PREFERRED_WORK_LENGTH = 1;
  public static final long MAX_PREFERRED_WORK_LENGTH = 300;
  // public static final long MIN_PREFERRED_BREAK_LENGTH = 1;
  // public static final long MAX_PREFERRED_BREAK_LENGTH = 90;
  public static final TemporalUnit UNIT_PREFERRED_WORK_LENGTH = ChronoUnit.MINUTES;

  public static Duration clampAndQuantize(Duration duration, long min, long max,
      TemporalUnit unit) {
    duration = duration.truncatedTo(unit);
    if (duration.compareTo(Duration.of(min, unit)) < 0)
      duration = Duration.of(min, unit);
    if (duration.compareTo(Duration.of(max, unit)) > 0)
      duration = Duration.of(max, unit);
    return duration;
  }
}
