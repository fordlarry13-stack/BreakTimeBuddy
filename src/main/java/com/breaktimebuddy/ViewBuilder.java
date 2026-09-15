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
  private final Runnable requestBreakRecommendationNow;
  private final Runnable acceptBreakRecommendation;
  private final Runnable rejectBreakRecommendation;

  public ViewBuilder(ViewModel model, Runnable toggleSession, Runnable saveConfig,
      Runnable loadConfig, Runnable requestBreakRecommendationNow,
      Runnable acceptBreakRecommendation, Runnable rejectBreakRecommendation) {
    this.viewModel = model;
    this.toggleSession = toggleSession;
    this.saveConfig = saveConfig;
    this.loadConfig = loadConfig;
    this.requestBreakRecommendationNow = requestBreakRecommendationNow;
    this.acceptBreakRecommendation = acceptBreakRecommendation;
    this.rejectBreakRecommendation = rejectBreakRecommendation;
  }

  @Override
  public Region build() {
    Label sampleLabel = new Label("Break Time Buddy - Project Started");
    Button sessionToggleButton = new Button();
    sessionToggleButton.setOnAction(e -> toggleSession.run());
    sessionToggleButton.textProperty().bind(viewModel.sessionStatusTextProperty());
    Label sessionsLabel = new Label();
    sessionsLabel.textProperty().bind(viewModel.sessionsProperty().asString("Sessions: %d"));
    Label breakRecommendationRequestedLabel = new Label();
    breakRecommendationRequestedLabel.textProperty().bind(Bindings.format(
        "Pending break recommendation: %s", viewModel.breakRecommendationRequestedProperty()));
    Button requestBreakRecommendationNowButton = new Button("Request break recommendation now");
    requestBreakRecommendationNowButton.setOnAction(e -> requestBreakRecommendationNow.run());
    DialogDisplay dialogDisplay;
    dialogDisplay = new DialogDisplay(requestBreakRecommendationNow, acceptBreakRecommendation,
        rejectBreakRecommendation);
    dialogDisplay.dialogStateProperty().bind(viewModel.dialogStateProperty());
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
    return new VBox(sampleLabel, sessionToggleButton, sessionsLabel,
        breakRecommendationRequestedLabel, requestBreakRecommendationNowButton, dialogDisplay,
        saveConfigButton, loadConfigButton, configFeedbackLabel);
  }
}
