package com.breaktimebuddy;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;

// TODO: Rename
public class ViewModel {
  private final BooleanProperty inSession = new SimpleBooleanProperty();
  private final IntegerProperty sessions = new SimpleIntegerProperty();

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
}
