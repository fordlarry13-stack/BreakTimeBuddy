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

        VBox root = new VBox(30);
        root.setStyle("-fx-background-color: #111; -fx-padding: 40;");

        // --- TITLE ---
        Label title = new Label("Break Time Buddy");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold;");

        // --- SESSION COUNTER ---
        Label sessionsLabel = new Label();
        sessionsLabel.textProperty().bind(viewModel.sessionsProperty().asString("Sessions: %d"));

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

            // SESSION VIEW
            Button breakButton = new Button("Take Break");
            breakButton.setStyle("-fx-background-color: white; -fx-text-fill: black;");
            breakButton.setOnAction(e -> viewModel.takeBreak());

            Button endButton = new Button("End Session");
            endButton.setStyle("-fx-background-color: white; -fx-text-fill: black;");
            endButton.setOnAction(e -> toggleSession.run());

            Label recTitle = new Label("AI Recommendation:");
            recTitle.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

            Label recommendationLabel = new Label("(No recommendation yet)");
            recommendationLabel.setStyle("-fx-text-fill: #ccc; -fx-font-size: 16px;");
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

            // MAIN VIEW
            Button startButton = new Button("Start Session");
            startButton.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-font-size: 16px;");
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