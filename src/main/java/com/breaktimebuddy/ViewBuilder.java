package com.breaktimebuddy;

import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Builder;
import javafx.util.converter.IntegerStringConverter;

/**
 * Builds the JavaFX view hierarchy.
 *
 * Binds UI controls to {@link ViewModel} properties and wires action handlers to controller
 * callbacks.
 */
public class ViewBuilder implements Builder<Region> {
  private final ViewModel viewModel;
  private final Runnable switchWorkBreak;
  private final Runnable saveConfig;
  private final Runnable loadConfig;
  private final Runnable requestBreakRecommendationNow;
  private final Runnable acceptBreakRecommendation;
  private final Runnable rejectBreakRecommendation;

  // Reference needed to prevent GC
  private final ObjectProperty<Integer> preferredWorkLengthInMinutesPropertyAsObject;

  /**
   * Creates a view builder with the given view model and action handlers.
   *
   * @param viewModel the view model to bind to
   * @param switchWorkBreak callback for toggling work/break session
   * @param saveConfig callback for saving configuration
   * @param loadConfig callback for loading configuration
   * @param requestBreakRecommendationNow callback for requesting a break recommendation
   * @param acceptBreakRecommendation callback for accepting a break recommendation
   * @param rejectBreakRecommendation callback for rejecting a break recommendation
   */
  public ViewBuilder(ViewModel viewModel, Runnable switchWorkBreak, Runnable saveConfig,
      Runnable loadConfig, Runnable requestBreakRecommendationNow,
      Runnable acceptBreakRecommendation, Runnable rejectBreakRecommendation) {
    this.viewModel = viewModel;
    this.switchWorkBreak = switchWorkBreak;
    this.saveConfig = saveConfig;
    this.loadConfig = loadConfig;
    this.requestBreakRecommendationNow = requestBreakRecommendationNow;
    this.acceptBreakRecommendation = acceptBreakRecommendation;
    this.rejectBreakRecommendation = rejectBreakRecommendation;

    preferredWorkLengthInMinutesPropertyAsObject =
        viewModel.preferredWorkLengthInMinutesProperty().asObject();
  }

  @Override
  public Region build() {
    VBox root = new VBox(20);
    Label title = new Label("Break Time Buddy");
    Button sessionToggleButton = new Button();
    sessionToggleButton.setOnAction(e -> switchWorkBreak.run());
    sessionToggleButton.textProperty().bind(viewModel.sessionStatusTextProperty());
    Label sessionsLabel = new Label();
    sessionsLabel.textProperty().bind(viewModel.sessionsProperty().asString("Sessions: %d"));
    Label preferredWorkLengthLabel, preferredWorkLengthLabelAfter = new Label(" minutes");
    Spinner<Integer> preferredWorkLengthSpinner;
    {
      int min = viewModel.getMinPreferredWorkLengthInMinutes(),
          max = viewModel.getMaxPreferredWorkLengthInMinutes(),
          default_ = viewModel.getDefaultPreferredWorkLengthInMinutes();
      preferredWorkLengthLabel = new Label("Preferred work length (%d-%d) : ".formatted(min, max));
      preferredWorkLengthSpinner = new Spinner<>(min, max, 0, 10);
      preferredWorkLengthSpinner.setPrefWidth(60);
      preferredWorkLengthSpinner.setEditable(true);
      Pattern filterPattern = Pattern.compile("\\d{0,%d}".formatted(String.valueOf(max).length()));
      TextFormatter<Integer> textFormatter = new TextFormatter<>(new IntegerStringConverter() {
        @Override
        public Integer fromString(String s) {
          try {
            return Math.min(
                Math.max(s == null || s.trim().isEmpty() ? default_ : Integer.parseInt(s), min),
                max);
          } catch (NumberFormatException e) {
            return preferredWorkLengthSpinner.valueProperty().getValue();
          }
        }
      }, preferredWorkLengthSpinner.valueProperty().getValue(),
          change -> filterPattern.matcher(change.getControlNewText()).matches() ? change : null);
      preferredWorkLengthSpinner.getEditor().setTextFormatter(textFormatter);
      preferredWorkLengthSpinner.getValueFactory().valueProperty()
          .bindBidirectional(textFormatter.valueProperty());
      preferredWorkLengthSpinner.getValueFactory().valueProperty()
          .bindBidirectional(preferredWorkLengthInMinutesPropertyAsObject);
    }
    HBox preferredWorkLengthContainer = new HBox(preferredWorkLengthLabel,
        preferredWorkLengthSpinner, preferredWorkLengthLabelAfter);
    Label historyLabel = new Label();
    historyLabel.textProperty()
        .bind(Bindings.createStringBinding(
            () -> String.format("History (%d):\n", viewModel.getHistory().size())
                + viewModel.getHistory().stream()
                    .map(e -> String.format("%s %s %s", e.phase(), e.beginTime(), e.endTime()))
                    .collect(Collectors.joining("\n")),
            viewModel.historyProperty()));
    Label breakRecommendationRequestedLabel = new Label();
    breakRecommendationRequestedLabel.textProperty().bind(Bindings.format(
        "Pending break recommendation: %s", viewModel.breakRecommendationRequestedProperty()));
    Button requestBreakRecommendationNowButton = new Button("Request break recommendation now");
    requestBreakRecommendationNowButton.setOnAction(e -> requestBreakRecommendationNow.run());
    DialogDisplay dialogDisplay;
    dialogDisplay = new DialogDisplay(acceptBreakRecommendation, rejectBreakRecommendation);
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
    root.getChildren().addAll(title, sessionToggleButton, sessionsLabel,
        preferredWorkLengthContainer, historyLabel, breakRecommendationRequestedLabel,
        requestBreakRecommendationNowButton, dialogDisplay, saveConfigButton, loadConfigButton,
        configFeedbackLabel);
    return root;
  }
}
