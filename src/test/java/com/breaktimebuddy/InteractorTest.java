/*-
Tests for Interactor class
Partially AI-generated: Test cases
 */
package com.breaktimebuddy;

import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.google.gson.JsonSyntaxException;

class InteractorTest {
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

    @Test
    void testInitialState() {
        // Initially not in session
        State state = stateChangeCaptor.lastState;
        assertFalse(state.inSession());
        assertEquals(0, state.sessions());
    }

    @Test
    void testSwitchWorkBreakStartsSession() {
        // Toggle to start session
        interactor.switchWorkBreak();
        // Should now be in session, sessions count unchanged (still 0)
        State state = stateChangeCaptor.lastState;
        assertTrue(state.inSession());
        assertEquals(0, state.sessions());
    }

    @Test
    void testSwitchWorkBreakEndsSessionIncrementsCount() {
        interactor.switchWorkBreak();
        // End the session (switchWorkBreak when in session)
        interactor.switchWorkBreak();
        // Sessions incremented when ending
        State state = stateChangeCaptor.lastState;
        assertFalse(state.inSession());
        assertEquals(1, state.sessions());
    }

    @Test
    void testSaveConfigCallsConfigHandlerWrite() throws IOException {
        // Set up state: end 3 sessions
        for (int i = 0; i < 6; i++)
            interactor.switchWorkBreak();
        interactor.saveConfig();
        ConfigData data = configHandler.getLastDataWritten();
        assertNotNull(data);
        assertEquals(3, data.sessions());
    }

    @Test
    void testLoadConfigCallsConfigHandlerRead() throws IOException, JsonSyntaxException {
        configHandler.setDataToReturn(new ConfigData(7));
        interactor.loadConfig();
        State state = stateChangeCaptor.lastState;
        assertEquals(7, state.sessions());
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
    void testLoadConfigThrowsJsonSyntaxExceptionWhenConfigHandlerThrowsJsonSyntaxException() {
        configHandler.setThrowOnReadJsonSyntaxException(true);
        assertThrows(JsonSyntaxException.class, () -> interactor.loadConfig());
    }

    @Test
    void testRecommendationRequestInvokesServiceAndDisplaysResult() {
        interactor.switchWorkBreak();

        interactor.requestBreakRecommendationNow();

        assertEquals(1, recommendationService.callCount);
        assertEquals(0, recommendationService.lastRequest.sessions());
        assertTrue(stateChangeCaptor.lastState.breakRecommendationRequested());

        recommendationService.lastFuture.complete("Take a short walk and stretch.");

        State state = stateChangeCaptor.lastState;
        assertFalse(state.breakRecommendationRequested());
        assertNotNull(state.dialogState());
        assertEquals("Take a short walk and stretch.", state.dialogState().message());
    }

    @Test
    void testRecommendationFailureClearsRequestWithoutOpeningDialog() {
        interactor.switchWorkBreak();
        interactor.requestBreakRecommendationNow();

        recommendationService.lastFuture
                .completeExceptionally(new RuntimeException("Simulated error"));

        State state = stateChangeCaptor.lastState;
        assertFalse(state.breakRecommendationRequested());
        assertNull(state.dialogState());
    }

    @Test
    void testSlowRecommendationCancelledAfterManualSwitch() {
        interactor.switchWorkBreak();
        interactor.requestBreakRecommendationNow();

        interactor.switchWorkBreak();
        recommendationService.lastFuture.complete("Slow recommendation response");

        assertTrue(recommendationService.lastFuture.isCancelled());
        State state = stateChangeCaptor.lastState;
        assertFalse(state.inSession());
        assertFalse(state.breakRecommendationRequested());
        assertNull(state.dialogState());
    }

    @Test
    void testRecommendationRequestTwice() {
        interactor.switchWorkBreak();
        interactor.requestBreakRecommendationNow();
        recommendationService.lastFuture.complete("Recommendation response 1");
        interactor.requestBreakRecommendationNow();
        recommendationService.lastFuture.complete("Recommendation response 2");

        assertEquals(2, recommendationService.callCount);
        State state = stateChangeCaptor.lastState;
        assertFalse(state.breakRecommendationRequested());
        assertNotNull(state.dialogState());
        assertEquals("Recommendation response 2", state.dialogState().message());
    }

    @Test
    void testNotInSessionBlocksRecommendation() {
        // Initially not in session
        interactor.requestBreakRecommendationNow();

        assertEquals(0, recommendationService.callCount);
    }

    @Test
    void testRecommendationDebouncing() {
        interactor.switchWorkBreak();
        interactor.requestBreakRecommendationNow();
        interactor.requestBreakRecommendationNow();

        assertEquals(1, recommendationService.callCount);
    }

    @Test
    void testAcceptBreakRecommendation() {
        interactor.switchWorkBreak();
        interactor.requestBreakRecommendationNow();
        recommendationService.lastFuture.complete("Recommendation response");
        UUID dialogId = stateChangeCaptor.lastState.dialogState().id();
        interactor.acceptBreakRecommendation(dialogId);

        State state = stateChangeCaptor.lastState;
        assertFalse(state.inSession());
        assertEquals(1, state.sessions());
        assertFalse(state.breakRecommendationRequested());
        assertNull(state.dialogState());
    }

    @Test
    void testAcceptBreakRecommendationIdempotent() {
        interactor.switchWorkBreak();
        interactor.requestBreakRecommendationNow();
        recommendationService.lastFuture.complete("Recommendation response");
        UUID dialogId = stateChangeCaptor.lastState.dialogState().id();
        interactor.acceptBreakRecommendation(dialogId);
        interactor.acceptBreakRecommendation(dialogId);

        State state = stateChangeCaptor.lastState;
        assertFalse(state.inSession());
        assertEquals(1, state.sessions());
        assertFalse(state.breakRecommendationRequested());
        assertNull(state.dialogState());
    }

    @Test
    void testRejectBreakRecommendation() {
        interactor.switchWorkBreak();
        interactor.requestBreakRecommendationNow();
        recommendationService.lastFuture.complete("Recommendation response");
        UUID dialogId = stateChangeCaptor.lastState.dialogState().id();
        interactor.rejectBreakRecommendation(dialogId);

        State state = stateChangeCaptor.lastState;
        assertTrue(state.inSession());
        assertEquals(0, state.sessions());
        assertFalse(state.breakRecommendationRequested());
        assertNull(state.dialogState());
    }

    @Test
    void testRejectBreakRecommendationIdempotent() {
        interactor.switchWorkBreak();
        interactor.requestBreakRecommendationNow();
        recommendationService.lastFuture.complete("Recommendation response");
        UUID dialogId = stateChangeCaptor.lastState.dialogState().id();
        interactor.rejectBreakRecommendation(dialogId);
        interactor.rejectBreakRecommendation(dialogId);

        State state = stateChangeCaptor.lastState;
        assertTrue(state.inSession());
        assertEquals(0, state.sessions());
        assertFalse(state.breakRecommendationRequested());
        assertNull(state.dialogState());
    }

    @Test
    void testManualSwitchClearsDialog() {
        interactor.switchWorkBreak();
        interactor.requestBreakRecommendationNow();
        recommendationService.lastFuture.complete("Recommendation response");

        assertNotNull(stateChangeCaptor.lastState.breakRecommendationRequested());

        interactor.switchWorkBreak();

        State state = stateChangeCaptor.lastState;
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

    /** A fake ConfigHandler for testing Interactor in isolation. */
    private static class FakeConfigHandler extends ConfigHandler {
        private ConfigData dataToReturn;
        private boolean throwOnReadIOException;
        private boolean throwOnReadJsonSyntaxException;
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
            this.dataToReturn = new ConfigData(0);
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

        public void setThrowOnReadJsonSyntaxException(boolean value) {
            this.throwOnReadJsonSyntaxException = value;
        }

        public void setThrowOnWrite(boolean value) {
            this.throwOnWrite = value;
        }

        @Override
        public ConfigData read() throws IOException, JsonSyntaxException {
            if (throwOnReadIOException)
                throw new IOException("Simulated read error");
            if (throwOnReadJsonSyntaxException)
                throw new JsonSyntaxException("Simulated read error");
            return dataToReturn;
        }

        @Override
        public void write(ConfigData data) throws IOException {
            if (throwOnWrite)
                throw new IOException("Simulated write error");
            this.lastDataWritten = data;
        }

    }

    private static class FakeRecommendationService implements RecommendationService {
        private int callCount;
        private RecommendationRequest lastRequest;
        private CompletableFuture<String> lastFuture = new CompletableFuture<>();

        @Override
        public CompletableFuture<String> getRecommendation(RecommendationRequest request) {
            callCount++;
            lastRequest = request;
            return lastFuture = new CompletableFuture<>();
        }
    }
}
