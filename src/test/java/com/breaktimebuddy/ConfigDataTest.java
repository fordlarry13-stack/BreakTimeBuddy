package com.breaktimebuddy;

import static org.junit.jupiter.api.Assertions.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class ConfigDataTest {
  @Test
  void testDefault() {
    ConfigData data = ConfigData.getDefault();
    assertEquals(0, data.sessions());
  }

  @Test
  void testSanitizeDataValid() {
    ConfigData originalData = new ConfigData(1,
        List.of(
            new ConfigData.HistoryItem(ConfigData.HistoryItem.Phase.WORK,
                Instant.ofEpochSecond(1, 2), Instant.ofEpochSecond(3, 4)),
            new ConfigData.HistoryItem(ConfigData.HistoryItem.Phase.BREAK,
                Instant.ofEpochSecond(5, 6), Instant.ofEpochSecond(7, 8))));
    ConfigData data = ConfigData.sanitize(originalData);
    assertFalse(originalData == data);
    assertEquals(originalData, data);
  }

  @Test
  void testSanitizeDataNullIsDefault() {
    assertEquals(ConfigData.getDefault(), ConfigData.sanitize(null));
  }

  @Test
  void testSanitizeDataNegativeSessions() {
    ConfigData data = ConfigData.sanitize(new ConfigData(-1, List.of()));
    assertEquals(0, data.sessions());
  }

  @Test
  void testSanitizeDataNullHistory() {
    ConfigData data = ConfigData.sanitize(new ConfigData(1, null));
    assertIterableEquals(List.of(), data.history());
  }

  @Test
  void testSanitizeDataLongHistory() {
    final int HISTORY_LENGTH = 20;
    List<ConfigData.HistoryItem> history = new ArrayList<>(HISTORY_LENGTH + 1);
    for (int i = 0; i < HISTORY_LENGTH + 1; i++)
      history.add(new ConfigData.HistoryItem(
          i % 2 == 0 ? ConfigData.HistoryItem.Phase.WORK : ConfigData.HistoryItem.Phase.BREAK,
          Instant.ofEpochSecond(i * 2), Instant.ofEpochSecond(i * 2 + 1)));
    ConfigData data = ConfigData.sanitize(new ConfigData(1, history));
    assertTrue(data.history().size() <= HISTORY_LENGTH);
  }

  @Test
  void testSanitizeDataFilterFailedTrySanitizeHistoryItem() {
    ConfigData data = ConfigData.sanitize(new ConfigData(1,
        Arrays.asList(new ConfigData.HistoryItem(ConfigData.HistoryItem.Phase.WORK,
            Instant.ofEpochSecond(1, 2), Instant.ofEpochSecond(3, 4)), null)));
    assertIterableEquals(Arrays.asList(new ConfigData.HistoryItem(ConfigData.HistoryItem.Phase.WORK,
        Instant.ofEpochSecond(1, 2), Instant.ofEpochSecond(3, 4))), data.history());
  }

  @Test
  void testTrySanitizeHistoryItemValid() {
    ConfigData.HistoryItem originalItem =
        new ConfigData.HistoryItem(ConfigData.HistoryItem.Phase.WORK, Instant.ofEpochSecond(1, 2),
            Instant.ofEpochSecond(3, 4));
    ConfigData.HistoryItem item = ConfigData.HistoryItem.trySanitize(originalItem);
    assertFalse(originalItem == item);
    assertEquals(originalItem, item);
  }

  @Test
  void testTrySanitizeHistoryItemNull() {
    ConfigData.HistoryItem item = ConfigData.HistoryItem.trySanitize(null);
    assertNull(item);
  }

  @Test
  void testTrySanitizeHistoryItemNullPhase() {
    ConfigData.HistoryItem item = ConfigData.HistoryItem.trySanitize(
        new ConfigData.HistoryItem(null, Instant.ofEpochSecond(1, 2), Instant.ofEpochSecond(3, 4)));
    assertNotNull(item);
    assertEquals(ConfigData.HistoryItem.Phase.WORK, item.phase());
  }

  @Test
  void testTrySanitizeHistoryItemNullBeginTime() {
    ConfigData.HistoryItem item = ConfigData.HistoryItem.trySanitize(new ConfigData.HistoryItem(
        ConfigData.HistoryItem.Phase.WORK, null, Instant.ofEpochSecond(3, 4)));
    assertNull(item);
  }

  @Test
  void testTrySanitizeHistoryItemNullEndTime() {
    ConfigData.HistoryItem item = ConfigData.HistoryItem.trySanitize(new ConfigData.HistoryItem(
        ConfigData.HistoryItem.Phase.WORK, Instant.ofEpochSecond(1, 2), null));
    assertNull(item);
  }

  @Test
  void testTrySanitizeHistoryItemBeginTimeNotBeforeEndTime() {
    ConfigData.HistoryItem item = ConfigData.HistoryItem
        .trySanitize(new ConfigData.HistoryItem(ConfigData.HistoryItem.Phase.WORK,
            Instant.ofEpochSecond(3, 4), Instant.ofEpochSecond(1, 2)));
    assertNull(item);
  }

  @Test
  void testTrySanitizeHistoryItemBeginTimeNotBeforeEndTimeBoundary() {
    ConfigData.HistoryItem item = ConfigData.HistoryItem
        .trySanitize(new ConfigData.HistoryItem(ConfigData.HistoryItem.Phase.WORK,
            Instant.ofEpochSecond(1, 2), Instant.ofEpochSecond(1, 2)));
    assertNull(item);
  }
}
