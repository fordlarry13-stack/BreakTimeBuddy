package com.breaktimebuddy;

import java.time.LocalTime;
import javafx.application.Platform;
import javafx.scene.layout.Region;

/**
 * Mediates between the view ({@link ViewBuilder}) and business logic ({@link Interactor}).
 *
 * Handles UI events, updates the {@link ViewModel}, and delegates to the interactor for state
 * changes and persistence.
 */
public class Controller {
  private final ViewModel viewModel;
  private final Interactor interactor;
  private final ViewBuilder viewBuilder;

  /**
   * Creates a controller with the given configuration handler and the default
   * {@link GroqRecommendationService}.
   *
   * @param configHandler the handler for reading and writing configuration
   */
  public Controller(ConfigHandler configHandler) {
    this(configHandler, new GroqRecommendationService());
  }

  /**
   * Creates a controller with the given dependencies.
   *
   * @param configHandler the handler for reading and writing configuration
   * @param recommendationService the service for generating break recommendations
   */
  private Controller(ConfigHandler configHandler, RecommendationService recommendationService) {
    viewModel = new ViewModel();
    interactor = new Interactor(state -> {
      if (Platform.isFxApplicationThread())
        updateModel(state);
      else
        Platform.runLater(() -> updateModel(state));
    }, configHandler, recommendationService);
    viewBuilder = new ViewBuilder(viewModel, this::switchWorkBreak, this::saveConfig,
        this::loadConfig, this::requestBreakRecommendationNow, this::acceptBreakRecommendation,
        this::rejectBreakRecommendation);
    viewModel.preferredWorkLengthProperty()
        .addListener((_0, _1, value) -> interactor.setPreferredWorkLength(value));
  }

  private void switchWorkBreak() {
    interactor.switchWorkBreak();
  }

  private void requestBreakRecommendationNow() {
    interactor.requestBreakRecommendationNow();
  }

  private void acceptBreakRecommendation() {
    interactor.acceptBreakRecommendation(viewModel.getDialogState().id());
  }

  private void rejectBreakRecommendation() {
    interactor.rejectBreakRecommendation(viewModel.getDialogState().id());
  }

  private void saveConfig() {
    // Synchronous code assuming fast config file access. Make this asynchronous if needed.
    try {
      interactor.saveConfig();
      viewModel.setConfigFeedbackTimestamp(LocalTime.now());
      viewModel.setConfigFeedbackMessage("Save success");
    } catch (Exception e) {
      viewModel.setConfigFeedbackTimestamp(LocalTime.now());
      viewModel.setConfigFeedbackMessage("Save error: " + e);
      e.printStackTrace();
    }
  }

  private void loadConfig() {
    // Synchronous code assuming fast config file access. Make this asynchronous if needed.
    try {
      interactor.loadConfig();
      viewModel.setConfigFeedbackTimestamp(LocalTime.now());
      viewModel.setConfigFeedbackMessage("Load success");
    } catch (Exception e) {
      viewModel.setConfigFeedbackTimestamp(LocalTime.now());
      viewModel.setConfigFeedbackMessage("Load error: " + e);
      e.printStackTrace();
    }
  }

  private void updateModel(State state) {
    viewModel.setInSession(state.inSession());
    viewModel.setSessions(state.sessions());
    viewModel.setPreferredWorkLength(state.preferredWorkLength());
    if (interactor == null)
      viewModel.setDefaultPreferredWorkLength(state.preferredWorkLength());
    viewModel.setHistory(state.history());
    viewModel.setBreakRecommendationRequested(state.breakRecommendationRequested());
    viewModel.setDialogState(state.dialogState());
  }

  /**
   * Returns the root view node built by the {@link ViewBuilder}.
   *
   * @return the view to display in the scene
   */
  public Region getView() {
    return viewBuilder.build();
  }
}
