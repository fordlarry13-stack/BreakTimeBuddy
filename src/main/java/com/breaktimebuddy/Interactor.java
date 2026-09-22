package com.breaktimebuddy;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import com.google.gson.JsonSyntaxException;

// TODO: Rename
public class Interactor {
  private record BreakRecommendationState(UUID id, String message) {
  }

  private final Consumer<State> stateChangeListener;
  private final ConfigHandler configHandler;
  private final RecommendationService recommendationService;

  private boolean inSession;
  private int sessions;
  private Duration preferredWorkLength;
  private AtomicBoolean breakRecommendationRequested = new AtomicBoolean();
  private CompletableFuture<String> currentBreakRecommendationFuture;
  private AtomicReference<BreakRecommendationState> breakRecommendationState =
      new AtomicReference<>();

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
    notifyStateChange();
  }

  public void setPreferredWorkLength(Duration preferredWorkLength) {
    this.preferredWorkLength =
        preferredWorkLength == null ? ConfigData.getDefault().preferredWorkLength()
            : PreferencesHelper.clampAndQuantize(preferredWorkLength,
                PreferencesHelper.MIN_PREFERRED_WORK_LENGTH,
                PreferencesHelper.MAX_PREFERRED_WORK_LENGTH,
                PreferencesHelper.UNIT_PREFERRED_WORK_LENGTH);
    notifyStateChange();
  }

  private void notifyStateChange() {
    if (stateChangeListener == null)
      return;
    BreakRecommendationState breakRecommendationState = this.breakRecommendationState.get();
    stateChangeListener.accept(new State(inSession, sessions, preferredWorkLength,
        breakRecommendationRequested.get(), breakRecommendationState == null ? null
            : new DialogState(breakRecommendationState.id(), breakRecommendationState.message())));
  }

  public void switchWorkBreak() {
    if (inSession)
      sessions++;
    setInSession(!inSession);
  }

  public void saveConfig() throws IOException {
    ConfigData data = new ConfigData(sessions, preferredWorkLength);
    configHandler.write(data);
  }

  public void loadConfig() throws IOException, JsonSyntaxException {
    spreadConfigData(configHandler.read());
  }

  private void spreadConfigData(ConfigData data) {
    sessions = data.sessions();
    preferredWorkLength = data.preferredWorkLength();
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
    CompletableFuture<String> future = recommendationService
        .getRecommendation(new RecommendationRequest(sessions, preferredWorkLength));
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

  public void requestBreakRecommendationNow() {
    requestBreakRecommendation();
  }

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

  public void rejectBreakRecommendation(UUID messageId) {
    breakRecommendationState.updateAndGet(state -> {
      if (state == null || !state.id().equals(messageId))
        return state;
      return null;
    });
    notifyStateChange();
  }
}
