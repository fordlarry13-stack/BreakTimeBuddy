package com.breaktimebuddy;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import com.google.gson.JsonSyntaxException;

/**
 * Core business logic for the application.
 *
 * Manages work/break session state, persists configuration, and coordinates break recommendations
 * with the {@link RecommendationService}.
 *
 * @see Controller
 * @see ViewModel
 */
public class Interactor {
  private record BreakRecommendationState(UUID id, String message) {
  }

  private final Consumer<State> stateChangeListener;
  private final ConfigHandler configHandler;
  private final RecommendationService recommendationService;

  private boolean inSession;
  private int sessions;
  private AtomicBoolean breakRecommendationRequested = new AtomicBoolean();
  private CompletableFuture<String> currentBreakRecommendationFuture;
  private AtomicReference<BreakRecommendationState> breakRecommendationState =
      new AtomicReference<>();

  /**
   * Creates an interactor with the given dependencies.
   *
   * @param stateChangeListener receives state updates
   * @param configHandler handles configuration persistence
   * @param recommendationService generates break recommendations
   */
  public Interactor(Consumer<State> stateChangeListener, ConfigHandler configHandler,
      RecommendationService recommendationService) {
    this.stateChangeListener = stateChangeListener;
    spreadConfigData(ConfigData.getDefault());
    this.configHandler = configHandler;
    this.recommendationService = recommendationService;
  }

  private void setInSession(boolean inSession) {
    this.inSession = inSession;
    if (!inSession) {
      clearAndCancelBreakRecommendationRequest(currentBreakRecommendationFuture);
      breakRecommendationState.set(null);
    }
  }

  private void notifyStateChange() {
    if (stateChangeListener == null)
      return;
    BreakRecommendationState breakRecommendationState = this.breakRecommendationState.get();
    stateChangeListener.accept(new State(inSession, sessions, breakRecommendationRequested.get(),
        breakRecommendationState == null ? null
            : new DialogState(breakRecommendationState.id(), breakRecommendationState.message())));
  }

  /**
   * Toggles between work and break sessions. Increments the session count when ending a work
   * session and notifies the state change listener.
   */
  public void switchWorkBreak() {
    if (inSession)
      sessions++;
    setInSession(!inSession);
    notifyStateChange();
  }

  /**
   * Saves the current session count to persistent storage.
   *
   * @throws IOException thrown if an I/O error occurs during write
   */
  public void saveConfig() throws IOException {
    ConfigData data = new ConfigData(sessions);
    configHandler.write(data);
  }

  /**
   * Loads the session count from persistent storage and updates the state.
   *
   * @throws IOException thrown if an I/O error occurs during read
   * @throws JsonSyntaxException thrown if the configuration file contains invalid JSON
   */
  public void loadConfig() throws IOException, JsonSyntaxException {
    spreadConfigData(configHandler.read());
  }

  private void spreadConfigData(ConfigData data) {
    sessions = data.sessions();
    notifyStateChange();
  }

  private void clearAndCancelBreakRecommendationRequest(CompletableFuture<String> future) {
    if (future != null && future == currentBreakRecommendationFuture) {
      breakRecommendationRequested.set(false);
      currentBreakRecommendationFuture = null;
      future.cancel(true);
    }
  }

  private void requestBreakRecommendation() {
    if (!inSession)
      return;
    if (!breakRecommendationRequested.compareAndSet(false, true))
      return;
    CompletableFuture<String> future =
        recommendationService.getRecommendation(new RecommendationRequest(sessions));
    currentBreakRecommendationFuture = future;
    future.whenComplete((message, error) -> {
      if (future != currentBreakRecommendationFuture)
        return;
      if (error == null && inSession)
        breakRecommendationState.set(new BreakRecommendationState(UUID.randomUUID(), message));
      clearAndCancelBreakRecommendationRequest(future);
      notifyStateChange();
    });
    notifyStateChange();
  }

  /**
   * Requests a break recommendation immediately, bypassing the normal timing logic.
   */
  public void requestBreakRecommendationNow() {
    requestBreakRecommendation();
  }

  /**
   * Accepts a break recommendation, ending the current work session and starting a break.
   *
   * @param messageId the ID of the recommendation
   */
  public void acceptBreakRecommendation(UUID messageId) {
    breakRecommendationState.updateAndGet(state -> {
      if (state == null || !state.id().equals(messageId))
        return state;
      if (inSession)
        switchWorkBreak();
      return null;
    });
    notifyStateChange();
  }

  /**
   * Rejects a break recommendation, dismissing it without starting a break.
   *
   * @param messageId the ID of the recommendation
   */
  public void rejectBreakRecommendation(UUID messageId) {
    breakRecommendationState.updateAndGet(state -> {
      if (state == null || !state.id().equals(messageId))
        return state;
      return null;
    });
    notifyStateChange();
  }
}
