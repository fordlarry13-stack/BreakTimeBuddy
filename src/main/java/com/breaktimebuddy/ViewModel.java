package com.breaktimebuddy;

import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

// TODO: Rename
public class ViewModel {
  private final BooleanProperty inSession = new SimpleBooleanProperty();
  private final IntegerProperty sessions = new SimpleIntegerProperty();
  private final ListProperty<HistoryItem> history =
      new SimpleListProperty<>(FXCollections.observableArrayList());

  public boolean getInSession() {
    return inSession.get();
  }

  public BooleanProperty isSessionProperty() {
    return inSession;
  }

  public void setInSession(boolean inSession) {
    this.inSession.set(inSession);
  }

  public int getSessions() {
    return sessions.get();
  }

  public IntegerProperty sessionsProperty() {
    return sessions;
  }

  public void setSessions(int sessions) {
    this.sessions.set(sessions);
  }

  public List<HistoryItem> getHistory() {
    return history;
  }

  public ListProperty<HistoryItem> historyProperty() {
    return history;
  }

  public void setHistory(List<HistoryItem> history) {
    this.history.setAll(history);
  }
}
