package com.breaktimebuddy;

import static org.junit.jupiter.api.Assertions.*;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;

class ConfigDataTest {
  @Test
  void testDefault() {
    ConfigData data = ConfigData.getDefault();
    assertEquals(0, data.sessions());
  }

  @Test
  void testSanitizeValid() {
    ConfigData originalData = new ConfigData(1, Duration.of(10, ChronoUnit.MINUTES));
    ConfigData data = ConfigData.sanitize(originalData);
    assertFalse(originalData == data);
    assertEquals(originalData, data);
  }

  @Test
  void testSanitizeNullDataIsDefault() {
    assertEquals(ConfigData.getDefault(), ConfigData.sanitize(null));
  }

  @Test
  void testSanitizeNegativeSessions() {
    ConfigData data = ConfigData.sanitize(new ConfigData(-1, Duration.of(10, ChronoUnit.MINUTES)));
    assertEquals(0, data.sessions());
  }

  @Test
  void testSanitizeNullPreferredWordLength() {
    ConfigData data = ConfigData.sanitize(new ConfigData(1, null));
    assertNotNull(data.preferredWorkLength());
  }

  @Test
  void testSanitizeSmallPreferredWordLength() {
    Duration limit = Duration.of(PreferencesHelper.MIN_PREFERRED_WORK_LENGTH,
        PreferencesHelper.UNIT_PREFERRED_WORK_LENGTH);
    ConfigData data = ConfigData.sanitize(new ConfigData(1, limit.dividedBy(2)));
    assertEquals(limit, data.preferredWorkLength());
  }

  @Test
  void testSanitizeLargePreferredWordLength() {
    Duration limit = Duration.of(PreferencesHelper.MAX_PREFERRED_WORK_LENGTH,
        PreferencesHelper.UNIT_PREFERRED_WORK_LENGTH);
    ConfigData data = ConfigData.sanitize(new ConfigData(1, limit.multipliedBy(2)));
    assertEquals(limit, data.preferredWorkLength());
  }

  @Test
  void testSanitizeUnquantizedPreferredWordLength() {
    Duration duration = Duration
        .of(PreferencesHelper.MIN_PREFERRED_WORK_LENGTH,
            PreferencesHelper.UNIT_PREFERRED_WORK_LENGTH)
        .plus(PreferencesHelper.UNIT_PREFERRED_WORK_LENGTH.getDuration().dividedBy(2));
    ConfigData data = ConfigData.sanitize(new ConfigData(1, duration));
    assertNotEquals(duration, data.preferredWorkLength());
  }
}
