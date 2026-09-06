package com.breaktimebuddy;

import java.io.File;
import java.io.IOException;

// TODO: Rename
public class Interactor {
  private final ViewModel viewModel;
  private boolean inSession;
  private int sessions;

  public Interactor(ViewModel model) {
    this.viewModel = model;
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

  private ConfigHandler configHandler = new ConfigHandler(new FileStorage(new File("config.json")));

  public void saveConfig() throws IOException {
    ConfigData data = new ConfigData(sessions);
    configHandler.write(data);
  }

  public void loadConfig() throws IOException {
    ConfigData data = configHandler.read();
    sessions = data.sessions();
  }
}
