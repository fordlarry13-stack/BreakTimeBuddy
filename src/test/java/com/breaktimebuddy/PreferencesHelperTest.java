package com.breaktimebuddy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.*;

class PreferencesHelperTest {
  @Test
  void testClampAndQuantizeValid() {
    assertEquals(Duration.ofSeconds(300),
        PreferencesHelper.clampAndQuantize(Duration.ofSeconds(300), 1, 10, ChronoUnit.MINUTES));
  }

  @Test
  void testClampAndQuantizeSmall() {
    assertEquals(Duration.ofSeconds(120),
        PreferencesHelper.clampAndQuantize(Duration.ofSeconds(60), 2, 10, ChronoUnit.MINUTES));
  }

  @Test
  void testClampAndQuantizeLarge() {
    assertEquals(Duration.ofSeconds(600),
        PreferencesHelper.clampAndQuantize(Duration.ofSeconds(900), 1, 10, ChronoUnit.MINUTES));
  }

  @Test
  void testClampAndQuantizeUnquantized() {
    assertEquals(Duration.ofSeconds(300),
        PreferencesHelper.clampAndQuantize(Duration.ofSeconds(330), 1, 10, ChronoUnit.MINUTES));
  }
}
