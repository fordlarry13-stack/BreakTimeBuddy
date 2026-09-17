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
            new ConfigData.HistoryItem(ConfigData.HistoryItem.Phase.WORK,
                    Instant.ofEpochSecond(1, 2), Instant.ofEpochSecond(3, 4)),
            new ConfigData.HistoryItem(ConfigData.HistoryItem.Phase.BREAK,
                    Instant.ofEpochSecond(5, 6), Instant.ofEpochSecond(7, 8)));

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
        assertEquals(List.of(), viewModel.getHistory());
    }

    @Test
    void testToggleSessionStartsSession() {
        // Toggle to start session
        interactor.toggleSession();
        interactor.updateModel();
        // Should now be in session, sessions count unchanged (still 0)
        assertTrue(viewModel.getInSession());
        assertEquals(0, viewModel.getSessions());
        assertEquals(List.of(), viewModel.getHistory());
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
        assertEquals(1, viewModel.getHistory().size());
        assertEquals(HistoryItem.Phase.WORK, viewModel.getHistory().get(0).phase());
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
        assertEquals(5, data.history().size());
        for (int i = 0; i < 5; i++)
            assertEquals(i % 2 == 0 ? ConfigData.HistoryItem.Phase.WORK
                    : ConfigData.HistoryItem.Phase.BREAK, data.history().get(i).phase());
    }

    @Test
    void testLoadConfigCallsConfigHandlerRead() throws IOException, JsonParseException {
        configHandler.setDataToReturn(new ConfigData(7, testHistoryData));
        interactor.loadConfig();
        interactor.updateModel();
        assertEquals(7, viewModel.getSessions());
        assertIterableEquals(testHistory, viewModel.getHistory());
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
