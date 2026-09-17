/*-
Tests for Interactor class
Partially AI-generated: Test cases
 */
package com.breaktimebuddy;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.google.gson.JsonParseException;

class InteractorTest {
    private ViewModel viewModel;
    private FakeConfigHandler configHandler;
    private Interactor interactor;

    @BeforeEach
    void setUp() {
        viewModel = new ViewModel();
        configHandler = new FakeConfigHandler();
        interactor = new Interactor(viewModel, configHandler);
    }

    @Test
    void testInitialState() {
        // Initially not in session
        assertFalse(viewModel.getInSession());
        assertEquals(0, viewModel.getSessions());
    }

    @Test
    void testToggleSessionStartsSession() {
        // Toggle to start session
        interactor.toggleSession();
        interactor.updateModel();
        // Should now be in session, sessions count unchanged (still 0)
        assertTrue(viewModel.getInSession());
        assertEquals(0, viewModel.getSessions());
    }

    @Test
    void testToggleSessionEndsSessionIncrementsCount() {
        interactor.toggleSession();
        // End the session (toggleSession when in session)
        interactor.toggleSession();
        interactor.updateModel();
        // Sessions incremented when ending
        assertFalse(viewModel.getInSession());
        assertEquals(1, viewModel.getSessions());
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
    void testLoadConfigCallsConfigHandlerRead() throws IOException, JsonParseException {
        configHandler.setDataToReturn(new ConfigData(7, null));
        interactor.loadConfig();
        interactor.updateModel();
        assertEquals(7, viewModel.getSessions());
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
