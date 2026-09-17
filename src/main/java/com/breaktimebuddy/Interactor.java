package com.breaktimebuddy;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import com.google.gson.JsonParseException;

// TODO: Rename
public class Interactor {
  private final Consumer<State> stateChangeListener;
  private final ConfigHandler configHandler;

  private boolean inSession;
  private int sessions;
  private final int HISTORY_LENGTH = 20;
  /** Newest first */
  private LinkedList<HistoryItem> history = new LinkedList<>();
  private HistoryItem.Open nextHistoryItem;

  public Interactor(Consumer<State> stateChangeListener, ConfigHandler configHandler) {
    this.stateChangeListener = stateChangeListener;
    spreadConfigData(ConfigData.getDefault());
    this.configHandler = configHandler;
  }

  private void notifyStateChange() {
    if (stateChangeListener == null)
      return;
    stateChangeListener.accept(new State(inSession, sessions, List.copyOf(history)));
  }

  public void toggleSession() {
    if (inSession)
      sessions++;
    inSession = !inSession;
    Instant current = Instant.now();
    if (nextHistoryItem != null) {
      if (history.size() >= HISTORY_LENGTH)
        history.removeLast();
      history.addFirst(nextHistoryItem.close(current));
    }
    nextHistoryItem =
        HistoryItem.open(inSession ? HistoryItem.Phase.WORK : HistoryItem.Phase.BREAK, current);
    notifyStateChange();
  }

  public void saveConfig() throws IOException {
    ConfigData data = new ConfigData(sessions,
        history.stream().map(e -> new ConfigData.HistoryItem(switch (e.phase()) {
          case WORK -> ConfigData.HistoryItem.Phase.WORK;
          case BREAK -> ConfigData.HistoryItem.Phase.BREAK;
        }, e.beginTime(), e.endTime())).toList());
    configHandler.write(data);
  }

  public void loadConfig() throws IOException, JsonParseException {
    spreadConfigData(configHandler.read());
  }

  private void spreadConfigData(ConfigData data) {
    sessions = data.sessions();
    history.clear();
    history.addAll(data.history().stream().map(e -> HistoryItem.open(switch (e.phase()) {
      case WORK -> HistoryItem.Phase.WORK;
      case BREAK -> HistoryItem.Phase.BREAK;
    }, e.beginTime()).close(e.endTime())).limit(HISTORY_LENGTH).toList());
    notifyStateChange();
  }
}
