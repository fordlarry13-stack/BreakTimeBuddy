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
  private final Runnable saveConfig;
  private final Runnable loadConfig;

  public ViewBuilder(ViewModel model, Runnable toggleSession, Runnable saveConfig,
      Runnable loadConfig) {
    this.viewModel = model;
    this.toggleSession = toggleSession;
    this.saveConfig = saveConfig;
    this.loadConfig = loadConfig;
  }

  @Override
  public Region build() {
    Label sampleLabel = new Label("Break Time Buddy - Project Started");
    Button sessionToggleButton = new Button();
    sessionToggleButton.setOnAction(e -> toggleSession.run());
    sessionToggleButton.textProperty().bind(viewModel.sessionStatusTextProperty());
    Label sessionsLabel = new Label();
    sessionsLabel.textProperty().bind(viewModel.sessionsProperty().asString("Sessions: %d"));
    Label configFeedbackLabel = new Label();
    configFeedbackLabel.textProperty()
        .bind(Bindings.createStringBinding(
            () -> viewModel.getConfigFeedbackMessage() == null ? ""
                : "[%s] %s".formatted(viewModel.getConfigFeedbackTimestamp(),
                    viewModel.getConfigFeedbackMessage()),
            viewModel.configFeedbackTimestampProperty(),
            viewModel.configFeedbackMessageProperty()));
    Button saveConfigButton = new Button("Save config");
    saveConfigButton.setOnAction(e -> saveConfig.run());
    Button loadConfigButton = new Button("Load config");
    loadConfigButton.setOnAction(e -> loadConfig.run());
    return new VBox(sampleLabel, sessionToggleButton, sessionsLabel, saveConfigButton,
        loadConfigButton, configFeedbackLabel);
  }
}
