package com.breaktimebuddy;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedList;
import com.google.gson.JsonSyntaxException;

// TODO: Rename
public class Interactor {
  private final ViewModel viewModel;
  private final ConfigHandler configHandler;

  private boolean inSession;
  private int sessions;
  private final int HISTORY_LENGTH = 20;
  /** Newest first */
  private LinkedList<HistoryItem> history = new LinkedList<>();
  private HistoryItem.Open nextHistoryItem;

  public Interactor(ViewModel model, ConfigHandler configHandler) {
    this.viewModel = model;
    this.configHandler = configHandler;
  }

  public void updateModel() {
    viewModel.setInSession(inSession);
    viewModel.setSessions(sessions);
    viewModel.setHistory(history);
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
  }

  public void saveConfig() throws IOException {
    ConfigData data = new ConfigData(sessions,
        history.stream().map(e -> new ConfigData.HistoryItem(switch (e.phase()) {
          case WORK -> ConfigData.HistoryItem.Phase.WORK;
          case BREAK -> ConfigData.HistoryItem.Phase.BREAK;
        }, e.beginTime(), e.endTime())).toList());
    configHandler.write(data);
  }

  public void loadConfig() throws IOException, JsonSyntaxException {
    ConfigData data = configHandler.read();
    sessions = data.sessions();
    history.clear();
    history.addAll(data.history().stream().map(e -> HistoryItem.open(switch (e.phase()) {
      case WORK -> HistoryItem.Phase.WORK;
      case BREAK -> HistoryItem.Phase.BREAK;
    }, e.beginTime()).close(e.endTime())).limit(HISTORY_LENGTH).toList());
  }
}
