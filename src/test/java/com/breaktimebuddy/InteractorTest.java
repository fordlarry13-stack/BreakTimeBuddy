/*-
Tests for Interactor class
Partially AI-generated: Test cases
 */
package com.breaktimebuddy;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.google.gson.JsonParseException;

class InteractorTest {
  private static final List<HistoryItem> testHistory = List.of(
      HistoryItem.open(HistoryItem.Phase.WORK, Instant.ofEpochSecond(1, 2))
          .close(Instant.ofEpochSecond(3, 4)),
      HistoryItem.open(HistoryItem.Phase.BREAK, Instant.ofEpochSecond(5, 6))
          .close(Instant.ofEpochSecond(7, 8)));
  private static final List<ConfigData.HistoryItem> testHistoryData = List.of(
      new ConfigData.HistoryItem(ConfigData.HistoryItem.Phase.WORK, Instant.ofEpochSecond(1, 2),
          Instant.ofEpochSecond(3, 4)),
      new ConfigData.HistoryItem(ConfigData.HistoryItem.Phase.BREAK, Instant.ofEpochSecond(5, 6),
          Instant.ofEpochSecond(7, 8)));

  private StateChangeCaptor stateChangeCaptor;
  private FakeConfigHandler configHandler;
  private FakeRecommendationService recommendationService;
  private Interactor interactor;

  @BeforeEach
  void setUp() {
    stateChangeCaptor = new StateChangeCaptor();
    configHandler = new FakeConfigHandler();
    recommendationService = new FakeRecommendationService();
    interactor = new Interactor(stateChangeCaptor, configHandler, recommendationService);
  }

  /**
   * Call {@link Interactor#switchWorkBreak}, then add a small delay. This is needed so session
   * start and end times are distinct.
   */
  void switchWorkBreakAndDelay() {
    interactor.switchWorkBreak();
    try {
      Thread.sleep(1);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @Test
  void testInitialState() {
    // Initially not in session
    State state = stateChangeCaptor.lastState;
    assertFalse(state.inSession());
    assertEquals(0, state.sessions());
    assertEquals(List.of(), state.history());
  }

  @Test
  void testSwitchWorkBreakStartsSession() {
    // Toggle to start session
    switchWorkBreakAndDelay();
    // Should nswitchWorkBreakAndDelayow be in session, sessions count unchanged (still 0)
    State state = stateChangeCaptor.lastState;
    assertTrue(state.inSession());
    assertEquals(0, state.sessions());
    assertEquals(List.of(), state.history());
  }

  @Test
  void testSwitchWorkBreakEndsSessionIncrementsCount() {
    switchWorkBreakAndDelay();
    // End the session (switchWorkBreak when in session)
    switchWorkBreakAndDelay();
    // Sessions incremented when ending
    State state = stateChangeCaptor.lastState;
    assertFalse(state.inSession());
    assertEquals(1, state.sessions());
    assertEquals(1, state.history().size());
    assertEquals(HistoryItem.Phase.WORK, state.history().get(0).phase());
  }

  @Test
  void testSwitchWorkBreakRemovesOldestItemOfLongHistory() throws InterruptedException {
    final int HISTORY_LENGTH = 20;
    // Fill the history
    for (int i = 0; i < HISTORY_LENGTH + 1; i++)
      switchWorkBreakAndDelay();

    State state = stateChangeCaptor.lastState;
    HistoryItem lastHistoryItem = state.history().get(state.history().size() - 1);
    assertEquals(HISTORY_LENGTH, state.history().size());
    assertTrue(state.history().contains(lastHistoryItem));

    switchWorkBreakAndDelay();

    state = stateChangeCaptor.lastState;
    assertEquals(HISTORY_LENGTH, state.history().size());
    assertFalse(state.history().contains(lastHistoryItem));
  }

  @Test
  void testSaveConfigCallsConfigHandlerWrite() throws IOException {
    // Set up state: end 3 sessions
    for (int i = 0; i < 6; i++)
      switchWorkBreakAndDelay();
    interactor.saveConfig();
    ConfigData data = configHandler.getLastDataWritten();
    assertNotNull(data);
    assertEquals(3, data.sessions());
    assertEquals(5, data.history().size());
    for (int i = 0; i < 5; i++)
      assertEquals(
          i % 2 == 0 ? ConfigData.HistoryItem.Phase.WORK : ConfigData.HistoryItem.Phase.BREAK,
          data.history().get(i).phase());
  }

  @Test
  void testLoadConfigCallsConfigHandlerRead() throws IOException, JsonParseException {
    configHandler.setDataToReturn(new ConfigData(7, testHistoryData));
    interactor.loadConfig();
    State state = stateChangeCaptor.lastState;
    assertEquals(7, state.sessions());
    assertIterableEquals(testHistory, state.history());
  }

  @Test
  void testSaveConfigThrowsIOExceptionWhenConfigHandlerThrowsIOException() {
    configHandler.setThrowOnWrite(true);
    assertThrows(IOException.class, () -> interactor.saveConfig());
  }

  @Test
  void testLoadConfigThrowsIOExceptionWhenConfigHandlerThrowsIOException() {
    configHandler.setThrowOnReadIOException(true);
    assertThrows(IOException.class, () -> interactor.loadConfig());
  }

  @Test
  void testLoadConfigThrowsJsonParseExceptionWhenConfigHandlerThrowsJsonParseException() {
    configHandler.setThrowOnReadJsonParseException(true);
    assertThrows(JsonParseException.class, () -> interactor.loadConfig());
  }

  @Test
  void testRecommendationRequestInvokesServiceAndDisplaysResult() {
    switchWorkBreakAndDelay();

    interactor.requestBreakRecommendationNow();

    assertEquals(1, recommendationService.callCount);
    assertEquals(0, recommendationService.lastRequest.sessions());
    assertTrue(stateChangeCaptor.lastState.breakRecommendationRequested());

    recommendationService.future.complete("Take a short walk and stretch.");

    State state = stateChangeCaptor.lastState;
    assertFalse(state.breakRecommendationRequested());
    assertNotNull(state.dialogState());
    assertEquals("Take a short walk and stretch.", state.dialogState().message());
  }

  @Test
  void testRecommendationFailureClearsRequestWithoutOpeningDialog() {
    switchWorkBreakAndDelay();
    interactor.requestBreakRecommendationNow();

    recommendationService.future.completeExceptionally(new RuntimeException("Simulated error"));

    State state = stateChangeCaptor.lastState;
    assertFalse(state.breakRecommendationRequested());
    assertNull(state.dialogState());
  }

  @Test
  void testStaleRecommendationDoesNotReopenDialogAfterManualSwitch() {
    recommendationService.future = new NonCancellableFuture();
    switchWorkBreakAndDelay();
    interactor.requestBreakRecommendationNow();

    switchWorkBreakAndDelay();
    recommendationService.future.complete("Stale recommendation");

    State state = stateChangeCaptor.lastState;
    assertFalse(state.inSession());
    assertFalse(state.breakRecommendationRequested());
    assertNull(state.dialogState());
  }

  private static class StateChangeCaptor implements Consumer<State> {
    private State lastState;

    @Override
    public void accept(State state) {
      lastState = state;
    }
  }

  private static class FakeRecommendationService implements RecommendationService {
    private int callCount;
    private RecommendationRequest lastRequest;
    private CompletableFuture<String> future = new CompletableFuture<>();

    @Override
    public CompletableFuture<String> getRecommendation(RecommendationRequest request) {
      callCount++;
      lastRequest = request;
      return future;
    }
  }

  private static class NonCancellableFuture extends CompletableFuture<String> {
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
      return false;
    }
  }

  /** A fake ConfigHandler for testing Interactor in isolation. */
  private static class FakeConfigHandler extends ConfigHandler {
    private ConfigData dataToReturn;
    private boolean throwOnReadIOException;
    private boolean throwOnReadJsonParseException;
    private boolean throwOnWrite;
    private ConfigData lastDataWritten;

    public FakeConfigHandler() {
      super(new Storage() {
        @Override
        public InputStream in() throws IOException {
          throw new UnsupportedOperationException("Unimplemented method 'in'");
        }

        @Override
        public OutputStream out() throws IOException {
          throw new UnsupportedOperationException("Unimplemented method 'out'");
        }
      }); // Anonymous subclass, won't be used
    }

    public void setDataToReturn(ConfigData data) {
      this.dataToReturn = data;
    }

    public ConfigData getLastDataWritten() {
      return lastDataWritten;
    }

    public void setThrowOnReadIOException(boolean value) {
      this.throwOnReadIOException = value;
    }

    public void setThrowOnReadJsonParseException(boolean value) {
      this.throwOnReadJsonParseException = value;
    }

    public void setThrowOnWrite(boolean value) {
      this.throwOnWrite = value;
    }

    @Override
    public ConfigData read() throws IOException, JsonParseException {
      if (throwOnReadIOException)
        throw new IOException("Simulated read error");
      if (throwOnReadJsonParseException)
        throw new JsonParseException("Simulated read error");
      if (dataToReturn == null)
        throw new IllegalStateException("dataToReturn can not be null");
      return dataToReturn;
    }

    @Override
    public void write(ConfigData data) throws IOException {
      if (throwOnWrite)
        throw new IOException("Simulated write error");
      this.lastDataWritten = data;
    }

  }
}
