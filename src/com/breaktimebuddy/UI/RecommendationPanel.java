package com.breaktimebuddy.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class RecommendationPanel {

    private Label recommendationLabel;

    public VBox getPanel() {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("AI Recommendation:");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        recommendationLabel = new Label("(No recommendation yet)");
        recommendationLabel.setStyle("-fx-text-fill: #ccc; -fx-font-size: 16px;");

        box.getChildren().addAll(title, recommendationLabel);

        return box;
    }

    public void update(String text) {
        recommendationLabel.setText(text);
    }
}