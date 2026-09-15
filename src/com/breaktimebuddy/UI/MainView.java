package com.breaktimebuddy.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class MainView {

    private Button startButton;
    private Button settingsButton;

    public VBox getView() {
        VBox root = new VBox(30);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #111; -fx-padding: 40;");

        Label title = new Label("Break Time Buddy");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold;");

        startButton = new Button("Start Session");
        startButton.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-font-size: 16px;");

        settingsButton = new Button("Settings");
        settingsButton.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-font-size: 16px;");

        root.getChildren().addAll(title, startButton, settingsButton);

        return root;
    }

    public Button getStartButton() {
            return startButton;
        }
    
        public Button getSettingsButton() {
            return settingsButton;
        }
    }