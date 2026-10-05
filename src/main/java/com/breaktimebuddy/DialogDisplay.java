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

/**
 * UI component that displays a break recommendation dialog.
 *
 * Shows the recommendation message and provides accept/reject buttons. Visibility is controlled by
 * binding to a {@link DialogState} property.
 */
public class DialogDisplay extends VBox {
  private final ObjectProperty<DialogState> dialogState = new SimpleObjectProperty<>();
  private final Label breakRecommendationLabel = new Label();

  /**
   * Creates a dialog display with the given action handlers.
   *
   * @param acceptBreakRecommendation runs when the accept button is clicked
   * @param rejectBreakRecommendation runs when the reject button is clicked
   */
  public DialogDisplay(Runnable acceptBreakRecommendation, Runnable rejectBreakRecommendation) {
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

  /**
   * Returns the current dialog state.
   *
   * @return the current dialog state, or null if none
   */
  public DialogState getDialogState() {
    return dialogState.get();
  }

  /**
   * Returns the property for the current dialog state.
   *
   * @return the dialog state property
   */
  public ObjectProperty<DialogState> dialogStateProperty() {
    return dialogState;
  }

  /**
   * Sets the current dialog state.
   *
   * @param dialogState the dialog state to set
   */
  public void setDialogState(DialogState dialogState) {
    this.dialogState.set(dialogState);
  }
}
