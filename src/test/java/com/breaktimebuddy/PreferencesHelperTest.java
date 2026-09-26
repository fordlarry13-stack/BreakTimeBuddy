package com.breaktimebuddy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.*;

class PreferencesHelperTest {
  @Test
  void testDefaultClampAndQuantizeValid() {
    assertEquals(Duration.ofSeconds(300), PreferencesHelper
        .defaultClampAndQuantize(Duration.ofSeconds(300), 5, 1, 10, ChronoUnit.MINUTES));
  }

  @Test
  void testDefaultClampAndQuantizeNull() {
    assertEquals(Duration.ofSeconds(300),
        PreferencesHelper.defaultClampAndQuantize(null, 5, 2, 10, ChronoUnit.MINUTES));
  }

  @Test
  void testDefaultClampAndQuantizeSmall() {
    assertEquals(Duration.ofSeconds(120), PreferencesHelper
        .defaultClampAndQuantize(Duration.ofSeconds(60), 5, 2, 10, ChronoUnit.MINUTES));
  }

  @Test
  void testDefaultClampAndQuantizeLarge() {
    assertEquals(Duration.ofSeconds(600), PreferencesHelper
        .defaultClampAndQuantize(Duration.ofSeconds(900), 5, 1, 10, ChronoUnit.MINUTES));
  }

  @Test
  void testDefaultClampAndQuantizeUnquantized() {
    assertEquals(Duration.ofSeconds(300), PreferencesHelper
        .defaultClampAndQuantize(Duration.ofSeconds(330), 5, 1, 10, ChronoUnit.MINUTES));
  }
}
