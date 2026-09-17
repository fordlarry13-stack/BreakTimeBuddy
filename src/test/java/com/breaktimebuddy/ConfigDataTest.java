package com.breaktimebuddy;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.Test;

class ConfigDataTest {
  @Test
  void testDefault() {
    ConfigData data = ConfigData.getDefault();
    assertEquals(0, data.sessions());
  }

  @Test
  void testSanitizeValid() {
    ConfigData originalData = new ConfigData(1, List.of());
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
    ConfigData data = ConfigData.sanitize(new ConfigData(-1, List.of()));
    assertEquals(0, data.sessions());
  }
}
