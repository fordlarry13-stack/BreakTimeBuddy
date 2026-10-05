package com.breaktimebuddy;

import java.time.Instant;

/**
 * An immutable data object representing a record of the beginning and ending times of a work or
 * break session. Construct the item in the following pattern:
 *
 * <pre>{@code
 * var phase = HistoryItem.Phase.WORK;
 * var beginTime = Instant.parse("2001-01-01T00:00:00Z");
 * var openItem = HistoryItem.open(phase, beginTime);
 * var endTime = Instant.parse("2001-01-01T00:45:00Z");
 * HistoryItem item = openItem.close(endTime);
 * }</pre>
 */
public class HistoryItem implements Cloneable {
  public static enum Phase {
    WORK, BREAK;
  }

  private Phase phase;
  private Instant beginTime;
  private Instant endTime;

  private HistoryItem() {}

  public Phase phase() {
    return this.phase;
  }

  public Instant beginTime() {
    return this.beginTime;
  }

  public Instant endTime() {
    return this.endTime;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj instanceof HistoryItem obj2)
      return this.phase().equals(obj2.phase()) && this.beginTime().equals(obj2.beginTime())
          && this.endTime().equals(obj2.endTime());
    return false;
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + phase().hashCode();
    result = 31 * result + beginTime().hashCode();
    result = 31 * result + endTime().hashCode();
    return result;
  }

  /**
   * A one-time builder for {@link HistoryItem}. Call {@link #close(Instant)} for the completed
   * object.
   *
   * The instance can't be reused; calling {@link #close(Instant)} more than once throws
   * {@link IllegalStateException}.
   */
  public static class Open implements Cloneable {
    private boolean open;
    private final HistoryItem item;

    private Open(Phase phase, Instant beginTime) {
      if (phase == null)
        throw new IllegalArgumentException("phase cannot be null");
      if (beginTime == null)
        throw new IllegalArgumentException("beginTime cannot be null");
      open = true;
      item = new HistoryItem();
      item.phase = phase;
      item.beginTime = beginTime;
    }

    /**
     * Returns whether this builder is still open (has not been closed).
     *
     * @return true if open, false if already closed
     */
    public boolean isOpen() {
      return open;
    }

    /**
     * Returns the phase of the session being built.
     *
     * @return the session phase
     */
    public Phase phase() {
      return this.item.phase;
    }

    /**
     * Returns the begin time of the session being built.
     *
     * @return the begin time
     */
    public Instant beginTime() {
      return this.item.beginTime;
    }

    /**
     * Completes the session with an end time and returns the finished {@link HistoryItem}.
     *
     * @param endTime the end time of the session
     * @return the completed history item
     * @throws IllegalStateException thrown when close has already been called
     * @throws IllegalArgumentException thrown when endTime is null
     */
    public HistoryItem close(Instant endTime) throws IllegalStateException {
      if (!open)
        throw new IllegalStateException("close called more than once");
      if (endTime == null)
        throw new IllegalArgumentException("endTime cannot be null");
      open = false;
      item.endTime = endTime;
      return item;
    }
  }

  /**
   * Creates a new open builder for a history item.
   *
   * @param phase the session phase (work or break)
   * @param beginTime the session begin time
   * @return a new open builder
   * @throws IllegalArgumentException thrown when phase or beginTime is null
   */
  public static Open open(Phase phase, Instant beginTime) {
    return new Open(phase, beginTime);
  }
}
