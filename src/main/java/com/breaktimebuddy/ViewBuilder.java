package com.breaktimebuddy;

import javafx.beans.binding.Bindings;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Builder;

// TODO: Rename
public class ViewBuilder implements Builder<Region> {
  private final ViewModel viewModel;
  private final Runnable toggleSession;

  public ViewBuilder(ViewModel model, Runnable toggleSession) {
    this.viewModel = model;
    this.toggleSession = toggleSession;
  }

  @Override
  public Region build() {
    Label sampleLabel = new Label("Break Time Buddy - Project Started");
    Button sessionToggleButton = new Button();
    sessionToggleButton.setOnAction(e -> toggleSession.run());
    sessionToggleButton.textProperty().bind(Bindings.when(viewModel.isSessionProperty())
        .then("In session").otherwise("Not in session"));
    Label sessionsLabel = new Label();
    sessionsLabel.textProperty().bind(viewModel.sessionsProperty().asString("Sessions: %d"));
    return new VBox(sampleLabel, sessionToggleButton, sessionsLabel);
  }
}
