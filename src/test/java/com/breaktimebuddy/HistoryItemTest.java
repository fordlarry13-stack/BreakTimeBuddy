package com.breaktimebuddy;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class HistoryItemTest {
  private static Instant beginTime;
  private static Instant endTime;

  @BeforeAll
  static void BeforeAll() {
    beginTime = Instant.parse("2001-01-01T00:00:00Z");
    endTime = Instant.parse("2001-01-01T00:45:00Z");
  }

  @Test
  void testOpenState() {
    var openItem = HistoryItem.open(HistoryItem.Phase.WORK, beginTime);
    assertTrue(openItem.isOpen());
    openItem.close(endTime);
    assertFalse(openItem.isOpen());
  }

  @Test
  void testWorkItemFields() {
    HistoryItem item = HistoryItem.open(HistoryItem.Phase.WORK, beginTime).close(endTime);
    assertEquals(HistoryItem.Phase.WORK, item.phase());
    assertEquals(beginTime, item.beginTime());
    assertEquals(endTime, item.endTime());
  }

  @Test
  void testBreakItemFields() {
    HistoryItem item = HistoryItem.open(HistoryItem.Phase.BREAK, beginTime).close(endTime);
    assertEquals(HistoryItem.Phase.BREAK, item.phase());
    assertEquals(beginTime, item.beginTime());
    assertEquals(endTime, item.endTime());
  }

  @Test
  void testThrowsIllegalStateExceptionWhenClosedTwice() {
    var openItem = HistoryItem.open(HistoryItem.Phase.WORK, beginTime);
    assertDoesNotThrow(() -> openItem.close(endTime));
    assertThrows(IllegalStateException.class, () -> openItem.close(endTime));
  }

  @Test
  void testThrowsIllegalArgumentExceptionWhenPhaseIsNull() {
    assertThrows(IllegalArgumentException.class, () -> HistoryItem.open(null, beginTime));
  }

  @Test
  void testThrowsIllegalArgumentExceptionWhenBeginTimeIsNull() {
    assertThrows(IllegalArgumentException.class,
        () -> HistoryItem.open(HistoryItem.Phase.WORK, null));
  }

  @Test
  void testThrowsIllegalArgumentExceptionWhenEndTimeIsNull() {
    var openItem = HistoryItem.open(HistoryItem.Phase.WORK, beginTime);
    assertThrows(IllegalArgumentException.class, () -> openItem.close(null));
  }
}
