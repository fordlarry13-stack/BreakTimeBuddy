package com.breaktimebuddy;

import java.io.IOException;
import com.google.gson.JsonSyntaxException;

// TODO: Rename
public class Interactor {
  private final ViewModel viewModel;
  private final ConfigHandler configHandler;

  private boolean inSession;
  private int sessions;

  public Interactor(ViewModel model, ConfigHandler configHandler) {
    this.viewModel = model;
    this.configHandler = configHandler;
  }

  public void updateModel() {
    viewModel.setInSession(inSession);
    viewModel.setSessions(sessions);
  }

  public void toggleSession() {
    if (inSession)
      sessions++;
    inSession = !inSession;
  }

  public void saveConfig() throws IOException {
    ConfigData data = new ConfigData(sessions);
    configHandler.write(data);
  }

  public void loadConfig() throws IOException, JsonSyntaxException {
    ConfigData data = configHandler.read();
    sessions = data.sessions();
  }
}
