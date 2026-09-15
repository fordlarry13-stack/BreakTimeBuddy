package com.breaktimebuddy.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class SessionView {

    private Label recommendationLabel;

    public VBox getView() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #111; -fx-padding: 40;");

        Label title = new Label("Session Active");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");

        Button breakBtn = new Button("Take Break");
        breakBtn.setStyle("-fx-background-color: white; -fx-text-fill: black;");

        Button endBtn = new Button("End Session");
        endBtn.setStyle("-fx-background-color: white; -fx-text-fill: black;");

        Label recTitle = new Label("AI Recommendation:");
        recTitle.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        recommendationLabel = new Label("(No recommendation yet)");
        recommendationLabel.setStyle("-fx-text-fill: #ccc; -fx-font-size: 16px;");

        root.getChildren().addAll(title, breakBtn, endBtn, recTitle, recommendationLabel);

        return root;
    } 
    
       // This is the hook Kapil's AI module will call
        public void showRecommendation(String text) {
            recommendationLabel.setText(text);
        }
    }