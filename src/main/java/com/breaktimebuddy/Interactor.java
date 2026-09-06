package com.breaktimebuddy;

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
}
