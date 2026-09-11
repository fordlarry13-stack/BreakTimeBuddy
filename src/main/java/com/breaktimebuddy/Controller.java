package com.breaktimebuddy;

import java.io.File;
import java.util.function.Consumer;
import javafx.scene.layout.Region;

// TODO: Rename
public class Controller {
  private final Interactor interactor;
  private final ViewBuilder viewBuilder;

  public Controller() {
    ViewModel model = new ViewModel();
    interactor = new Interactor(model, new ConfigHandler(new FileStorage(new File("config.json"))));
    viewBuilder = new ViewBuilder(model, this::toggleSession, this::saveConfig, this::loadConfig);
  }

  private void toggleSession() {
    interactor.toggleSession();
    interactor.updateModel();
  }

  private void saveConfig(Consumer<Throwable> callback) {
    // Synchronous code assuming fast config file access. Make this asynchronous if needed.
    try {
      interactor.saveConfig();
      callback.accept(null);
    } catch (Exception e) {
      callback.accept(e);
    }
  }

  private void loadConfig(Consumer<Throwable> callback) {
    // Synchronous code assuming fast config file access. Make this asynchronous if needed.
    try {
      interactor.loadConfig();
      interactor.updateModel();
      callback.accept(null);
    } catch (Exception e) {
      callback.accept(e);
    }
  }

  public Region getView() {
    return viewBuilder.build();
  }
}
