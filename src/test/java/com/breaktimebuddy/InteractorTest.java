/*-
Tests for Interactor class
Partially AI-generated: Test cases
 */
package com.breaktimebuddy;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.google.gson.JsonSyntaxException;

class InteractorTest {
    private StateChangeCaptor stateChangeCaptor;
    private FakeConfigHandler configHandler;
    private Interactor interactor;

    @BeforeEach
    void setUp() {
        stateChangeCaptor = new StateChangeCaptor();
        configHandler = new FakeConfigHandler();
        interactor = new Interactor(stateChangeCaptor, configHandler);
    }

    @Test
    void testInitialState() {
        // Initially not in session
        State state = stateChangeCaptor.lastState;
        assertFalse(state.inSession());
        assertEquals(0, state.sessions());
    }

    @Test
    void testToggleSessionStartsSession() {
        // Toggle to start session
        interactor.toggleSession();
        // Should now be in session, sessions count unchanged (still 0)
        State state = stateChangeCaptor.lastState;
        assertTrue(state.inSession());
        assertEquals(0, state.sessions());
    }

    @Test
    void testToggleSessionEndsSessionIncrementsCount() {
        interactor.toggleSession();
        // End the session (toggleSession when in session)
        interactor.toggleSession();
        // Sessions incremented when ending
        State state = stateChangeCaptor.lastState;
        assertFalse(state.inSession());
        assertEquals(1, state.sessions());
    }

    @Test
    void testSaveConfigCallsConfigHandlerWrite() throws IOException {
        // Set up state: end 3 sessions
        for (int i = 0; i < 6; i++)
            interactor.toggleSession();
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
}
