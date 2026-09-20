package com.breaktimebuddy;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import com.google.gson.JsonParseException;

// TODO: Rename
public class Interactor {
  private record BreakRecommendationState(UUID id, String message) {
  }

  private final Consumer<State> stateChangeListener;
  private final ConfigHandler configHandler;
  private final RecommendationService recommendationService;

  private boolean inSession;
  private int sessions;
  private static final int HISTORY_LENGTH = 20;
  /** Newest first */
  private LinkedList<HistoryItem> history = new LinkedList<>();
  private HistoryItem.Open nextHistoryItem;
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
  }

  private void notifyStateChange() {
    if (stateChangeListener == null)
      return;
    BreakRecommendationState breakRecommendationState = this.breakRecommendationState.get();
    stateChangeListener.accept(new State(inSession, sessions, List.copyOf(history),
        breakRecommendationRequested.get(), breakRecommendationState == null ? null
            : new DialogState(breakRecommendationState.id(), breakRecommendationState.message())));
  }

  public void switchWorkBreak() {
    if (inSession)
      sessions++;
    setInSession(!inSession);
    Instant current = Instant.now();
    if (nextHistoryItem != null) {
      if (history.size() >= HISTORY_LENGTH)
        history.removeLast();
      history.addFirst(nextHistoryItem.close(current));
    }
    nextHistoryItem =
        HistoryItem.open(inSession ? HistoryItem.Phase.WORK : HistoryItem.Phase.BREAK, current);
    notifyStateChange();
  }

  public void saveConfig() throws IOException {
    ConfigData data = new ConfigData(sessions,
        history.stream().map(e -> new ConfigData.HistoryItem(switch (e.phase()) {
          case WORK -> ConfigData.HistoryItem.Phase.WORK;
          case BREAK -> ConfigData.HistoryItem.Phase.BREAK;
        }, e.beginTime(), e.endTime())).toList());
    configHandler.write(data);
  }

  public void loadConfig() throws IOException, JsonParseException {
    spreadConfigData(configHandler.read());
  }

  private void spreadConfigData(ConfigData data) {
    sessions = data.sessions();
    history.clear();
    history.addAll(data.history().stream().map(e -> HistoryItem.open(switch (e.phase()) {
      case WORK -> HistoryItem.Phase.WORK;
      case BREAK -> HistoryItem.Phase.BREAK;
    }, e.beginTime()).close(e.endTime())).limit(HISTORY_LENGTH).toList());
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
        recommendationService.getRecommendation(new RecommendationRequest(sessions,
            Duration.between(nextHistoryItem.beginTime(), Instant.now()), List.copyOf(history)));
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
