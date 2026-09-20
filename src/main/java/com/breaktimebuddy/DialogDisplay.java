package com.breaktimebuddy;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class DialogDisplay extends VBox {
  private final ObjectProperty<DialogState> dialogState = new SimpleObjectProperty<>();
  private final Label breakRecommendationLabel = new Label();

  public DialogDisplay(Runnable requestBreakRecommendationNow, Runnable acceptBreakRecommendation,
      Runnable rejectBreakRecommendation) {
    super();
    dialogState.addListener((_0, _1, value) -> handleDialogStateChange(value));
    handleDialogStateChange(getDialogState());
    Button acceptBreakRecommendationButton = new Button("Start Break");
    acceptBreakRecommendationButton.setOnAction(e -> acceptBreakRecommendation.run());
    Button rejectBreakRecommendationButton = new Button("Not Now");
    rejectBreakRecommendationButton.setOnAction(e -> rejectBreakRecommendation.run());
    setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY,
        BorderWidths.DEFAULT)));
    getChildren().addAll(breakRecommendationLabel, acceptBreakRecommendationButton,
        rejectBreakRecommendationButton);
  }

  private void handleDialogStateChange(DialogState value) {
    if (value == null) {
      setVisible(false);
    } else {
      setVisible(true);
      breakRecommendationLabel.setText("Recommendation: %s".formatted(value.message()));
    }
  }

  public DialogState getDialogState() {
    return dialogState.get();
  }

  public ObjectProperty<DialogState> dialogStateProperty() {
    return dialogState;
  }

  public void setDialogState(DialogState dialogState) {
    this.dialogState.set(dialogState);
  }
}
