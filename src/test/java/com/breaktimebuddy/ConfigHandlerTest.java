/*
 * AI-generated: Tests for ConfigHandler class
 */
package com.breaktimebuddy;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConfigHandlerTest {

    private Storage storage;
    private ConfigHandler handler;

    @BeforeEach
    void setUp() {
        storage = new FakeStorage();
        handler = new ConfigHandler(storage);
    }

    @Test
    void testReadReturnsConfigData() throws IOException {
        // Arrange: storage provides valid JSON for ConfigData with sessions = 5
        String json = "{\"sessions\":5}";
        ((FakeStorage) storage).setInputData(json);

        // Act
        ConfigData data = handler.read();

        // Assert
        assertNotNull(data);
        assertEquals(5, data.sessions());
    }

    @Test
    void testWriteWritesJson() throws IOException {
        // Arrange
        ConfigData data = new ConfigData(10);
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        ((FakeStorage) storage).setOutputCaptor(outBytes);

        // Act
        handler.write(data);

        // Assert
        String written = outBytes.toString(StandardCharsets.UTF_8.name());
        // The written JSON should contain sessions:10
        assertTrue(written.contains("\"sessions\":10"));
    }

    @Test
    void testReadThrowsIOExceptionWhenStorageThrows() throws IOException {
        // Arrange
        storage = new Storage() {
            @Override
            public InputStream in() throws IOException {
                throw new IOException("forced error");
            }

            @Override
            public OutputStream out() throws IOException {
                return new ByteArrayOutputStream();
            }
        };
        handler = new ConfigHandler(storage);

        // Act & Assert
        assertThrows(IOException.class, () -> handler.read());
    }

    @Test
    void testWriteThrowsIOExceptionWhenStorageThrows() throws IOException {
        // Arrange
        ConfigData data = new ConfigData(1);
        storage = new Storage() {
            @Override
            public InputStream in() throws IOException {
                return new ByteArrayInputStream(new byte[0]);
            }

            @Override
            public OutputStream out() throws IOException {
                throw new IOException("forced error");
            }
        };
        handler = new ConfigHandler(storage);

        // Act & Assert
        assertThrows(IOException.class, () -> handler.write(data));
    }

    /** A simple fake Storage for testing. */
    private static class FakeStorage implements Storage {
        private String inputData;
        private ByteArrayOutputStream outputCaptor;
        private boolean throwOnIn;
        private boolean throwOnOut;

        public void setInputData(String json) {
            this.inputData = json;
        }

        public void setOutputCaptor(ByteArrayOutputStream outBytes) {
            this.outputCaptor = outBytes;
        }

        @Override
        public InputStream in() throws IOException {
            if (throwOnIn) {
                throw new IOException("Simulated input error");
            }
            return new ByteArrayInputStream(inputData.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public OutputStream out() throws IOException {
            if (throwOnOut) {
                throw new IOException("Simulated output error");
            }
            return new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    outputCaptor.write(b);
                }
            };
        }
    }
}
