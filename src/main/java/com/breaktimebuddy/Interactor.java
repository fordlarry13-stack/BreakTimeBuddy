package com.breaktimebuddy;

import java.io.IOException;
import java.util.function.Consumer;
import com.google.gson.JsonSyntaxException;

// TODO: Rename
public class Interactor {
  private final Consumer<State> stateChangeListener;
  private final ConfigHandler configHandler;

  private boolean inSession;
  private int sessions;

  public Interactor(Consumer<State> stateChangeListener, ConfigHandler configHandler) {
    this.stateChangeListener = stateChangeListener;
    notifyStateChange();
    this.configHandler = configHandler;
  }

  private void notifyStateChange() {
    if (stateChangeListener == null)
      return;
    stateChangeListener.accept(new State(inSession, sessions));
  }

  public void toggleSession() {
    if (inSession)
      sessions++;
    inSession = !inSession;
    notifyStateChange();
  }

  public void saveConfig() throws IOException {
    ConfigData data = new ConfigData(sessions);
    configHandler.write(data);
  }

  public void loadConfig() throws IOException, JsonSyntaxException {
    ConfigData data = configHandler.read();
    sessions = data.sessions();
    notifyStateChange();
  }
}
