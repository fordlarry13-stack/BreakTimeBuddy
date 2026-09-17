package com.breaktimebuddy;

import java.time.LocalTime;
import java.util.function.Consumer;
import java.util.stream.Collectors;
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
  private final Consumer<Consumer<Throwable>> saveConfig;
  private final Consumer<Consumer<Throwable>> loadConfig;

  public ViewBuilder(ViewModel model, Runnable toggleSession,
      Consumer<Consumer<Throwable>> saveConfig, Consumer<Consumer<Throwable>> loadConfig) {
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
    sessionToggleButton.textProperty().bind(Bindings.when(viewModel.isSessionProperty())
        .then("In session").otherwise("Not in session"));
    Label sessionsLabel = new Label();
    sessionsLabel.textProperty().bind(viewModel.sessionsProperty().asString("Sessions: %d"));
    Label historyLabel = new Label();
    historyLabel.textProperty()
        .bind(Bindings.createStringBinding(
            () -> String.format("History (%d):\n", viewModel.getHistory().size())
                + viewModel.getHistory().stream()
                    .map(e -> String.format("%s %s %s", e.phase(), e.beginTime(), e.endTime()))
                    .collect(Collectors.joining("\n")),
            viewModel.historyProperty()));
    Label configFeedbackLabel = new Label();
    Button saveConfigButton = new Button("Save config");
    saveConfigButton.setOnAction(e -> saveConfig.accept(error -> {
      if (error == null)
        configFeedbackLabel.setText(String.format("[%s]: Save success", LocalTime.now()));
      else {
        configFeedbackLabel.setText(String.format("[%s]: Save error: %s", LocalTime.now(), error));
        error.printStackTrace();
      }
    }));
    Button loadConfigButton = new Button("Load config");
    loadConfigButton.setOnAction(e -> loadConfig.accept(error -> {
      if (error == null)
        configFeedbackLabel.setText(String.format("[%s]: Load success", LocalTime.now()));
      else {
        configFeedbackLabel.setText(String.format("[%s]: Load error: %s", LocalTime.now(), error));
        error.printStackTrace();
      }
    }));
    return new VBox(sampleLabel, sessionToggleButton, sessionsLabel, historyLabel, saveConfigButton,
        loadConfigButton, configFeedbackLabel);
  }
}
