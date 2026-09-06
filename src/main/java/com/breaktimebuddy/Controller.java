package com.breaktimebuddy;

import javafx.scene.layout.Region;

// TODO: Rename
public class Controller {
  private final Interactor interactor;
  private final ViewBuilder viewBuilder;

  public Controller() {
    ViewModel model = new ViewModel();
    interactor = new Interactor(model);
    viewBuilder = new ViewBuilder(model, this::toggleSession);
  }

  private void toggleSession() {
    interactor.toggleSession();
    interactor.updateModel();
  }

  public Region getView() {
    return viewBuilder.build();
  }
}
