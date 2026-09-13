package com.breaktimebuddy;

import java.io.File;
import java.time.LocalTime;
import javafx.application.Platform;
import javafx.scene.layout.Region;

// TODO: Rename
public class Controller {
  private final ViewModel viewModel;
  private final Interactor interactor;
  private final ViewBuilder viewBuilder;

  public Controller() {
    viewModel = new ViewModel();
    interactor = new Interactor(state -> Platform.runLater(() -> updateModel(state)),
        new ConfigHandler(new FileStorage(new File("config.json"))));
    viewBuilder =
        new ViewBuilder(viewModel, this::toggleSession, this::saveConfig, this::loadConfig);
  }

  private void toggleSession() {
    interactor.toggleSession();
  }

  private void saveConfig() {
    // Synchronous code assuming fast config file access. Make this asynchronous if needed.
    try {
      interactor.saveConfig();
      viewModel.setConfigFeedbackTimestamp(LocalTime.now());
      viewModel.setConfigFeedbackMessage("Save success");
    } catch (Exception e) {
      viewModel.setConfigFeedbackTimestamp(LocalTime.now());
      viewModel.setConfigFeedbackMessage("Save error: " + e);
      e.printStackTrace();
    }
  }

  private void loadConfig() {
    // Synchronous code assuming fast config file access. Make this asynchronous if needed.
    try {
      interactor.loadConfig();
      viewModel.setConfigFeedbackTimestamp(LocalTime.now());
      viewModel.setConfigFeedbackMessage("Load success");
    } catch (Exception e) {
      viewModel.setConfigFeedbackTimestamp(LocalTime.now());
      viewModel.setConfigFeedbackMessage("Load error: " + e);
      e.printStackTrace();
    }
  }

  private void updateModel(State state) {
    viewModel.setInSession(state.inSession());
    viewModel.setSessions(state.sessions());
  }

  public Region getView() {
    return viewBuilder.build();
  }
}
