package com.breaktimebuddy;

import javafx.beans.binding.Bindings;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Builder;

public class ViewBuilder implements Builder<Region> {

    private final ViewModel viewModel;
    private final Runnable toggleSession;
    private final Runnable saveConfig;
    private final Runnable loadConfig;

    public ViewBuilder(ViewModel model,
                       Runnable toggleSession,
                       Runnable saveConfig,
                       Runnable loadConfig) {

        this.viewModel = model;
        this.toggleSession = toggleSession;
        this.saveConfig = saveConfig;
        this.loadConfig = loadConfig;
    }

    @Override
    public Region build() {

        VBox root = new VBox(20);

        // --- TITLE ---
        Label title = new Label("Break Time Buddy");

        // --- SESSION COUNTER ---
        Label sessionsLabel = new Label();
        sessionsLabel.textProperty().bind(
                viewModel.sessionsProperty().asString("Sessions: %d")
        );

        // --- CONFIG BUTTONS ---
        Button saveConfigButton = new Button("Save config");
        saveConfigButton.setOnAction(e -> saveConfig.run());

        Button loadConfigButton = new Button("Load config");
        loadConfigButton.setOnAction(e -> loadConfig.run());

        Label configFeedbackLabel = new Label();
        configFeedbackLabel.textProperty().bind(
                Bindings.createStringBinding(
                        () -> viewModel.getConfigFeedbackMessage() == null
                                ? ""
                                : "[%s] %s".formatted(
                                        viewModel.getConfigFeedbackTimestamp(),
                                        viewModel.getConfigFeedbackMessage()
                                ),
                        viewModel.configFeedbackTimestampProperty(),
                        viewModel.configFeedbackMessageProperty()
                )
        );

        // --- CONDITIONAL VIEW SWITCHING ---
        if (viewModel.isSessionActive()) {

            // SESSION VIEW (visual order)
            Button breakButton = new Button("Take Break");
            breakButton.setOnAction(e -> viewModel.takeBreak());

            Button endButton = new Button("End Session");
            endButton.setOnAction(e -> toggleSession.run());

            Label recTitle = new Label("AI Recommendation:");
            Label recommendationLabel = new Label();
            recommendationLabel.textProperty().bind(viewModel.aiRecommendationProperty());

            root.getChildren().addAll(
                    title,
                    breakButton,
                    endButton,
                    recTitle,
                    recommendationLabel,
                    sessionsLabel,
                    saveConfigButton,
                    loadConfigButton,
                    configFeedbackLabel
            );

        } else {

            // MAIN VIEW (visual order)
            Button startButton = new Button("Start Session");
            startButton.setOnAction(e -> toggleSession.run());

            root.getChildren().addAll(
                    title,
                    startButton,
                    sessionsLabel,
                    saveConfigButton,
                    loadConfigButton,
                    configFeedbackLabel
            );
        }

        return root;
    }
}