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
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.google.gson.JsonSyntaxException;

class ConfigHandlerTest {

  private FakeStorage storage;
  private ConfigHandler handler;

  @BeforeEach
  void setUp() {
    storage = new FakeStorage();
    handler = new ConfigHandler(storage);
  }

  @Test
  void testReadReturnsConfigData() throws IOException, JsonSyntaxException {
    // Arrange: storage provides valid JSON for ConfigData
    String json = "{\"sessions\":5,\"preferredWorkLength\":{\"seconds\":600,\"nanos\":0}}";
    storage.setInputData(json);

    // Act
    ConfigData data = handler.read();

    // Assert
    assertNotNull(data);
    assertEquals(5, data.sessions());
    assertEquals(Duration.ofSeconds(600), data.preferredWorkLength());
  }

  @Test
  void testReadReturnsConfigDataWithDefaults() throws IOException, JsonSyntaxException {
    // Arrange: storage provides empty JSON, which becomes null
    storage.setInputData("");

    // Act
    ConfigData data = handler.read();

    // Assert: every value is recursively filled with defaults
    assertNotNull(data);
    assertEquals(ConfigData.getDefault(), data);
  }

  /**
   * This tests that the data is sanitized at all. See {@link ConfigDataTest} for more sanitization
   * tests.
   */
  @Test
  void testReadReturnsSanitizedConfigData() throws IOException, JsonSyntaxException {
    // Arrange: storage provides valid JSON but with invalid data
    storage.setInputData("{sessions:-1}");

    // Act
    ConfigData data = handler.read();

    // Assert: every value is recursively filled with defaults
    assertNotNull(data);
    assertEquals(0, data.sessions());
  }

  @Test
  void testWriteWritesJson() throws IOException {
    // Arrange
    ConfigData data = new ConfigData(10, Duration.of(10, ChronoUnit.MINUTES));
    ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
    storage.setOutputCaptor(outBytes);

    // Act
    handler.write(data);

    // Assert
    String written = outBytes.toString(StandardCharsets.UTF_8.name());

    assertTrue(written.contains("\"sessions\":10"));
    assertTrue(written.contains("\"preferredWorkLength\":{\"seconds\":600,\"nanos\":0}"));
  }

  @Test
  void testReadThrowsIOExceptionWhenStorageThrows() throws IOException, JsonSyntaxException {
    // Arrange: Simulate input failure
    storage.setThrowOnIn(true);

    // Act & Assert
    assertThrows(IOException.class, () -> handler.read());
  }

  @Test
  void testWriteThrowsIOExceptionWhenStorageThrows() throws IOException {
    // Arrange: Simulate output failure
    storage.setThrowOnOut(true);
    ConfigData data = new ConfigData(1, Duration.of(10, ChronoUnit.MINUTES));

    // Act & Assert
    assertThrows(IOException.class, () -> handler.write(data));
  }

  @Test
  void testReadThrowsJsonSyntaxExceptionWhenInvalidJson() throws IOException {
    // Arrange: storage provides invalid JSON
    String invalidJson = "{ invalid json }";
    storage.setInputData(invalidJson);

    // Act & Assert
    assertThrows(JsonSyntaxException.class, () -> handler.read());
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

    public void setThrowOnIn(boolean value) {
      this.throwOnIn = value;
    }

    public void setThrowOnOut(boolean value) {
      this.throwOnOut = value;
    }

    @Override
    public InputStream in() throws IOException {
      if (throwOnIn)
        throw new IOException("Simulated input error");
      return new ByteArrayInputStream(inputData.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public OutputStream out() throws IOException {
      if (throwOnOut)
        throw new IOException("Simulated output error");
      return new OutputStream() {
        @Override
        public void write(int b) throws IOException {
          outputCaptor.write(b);
        }
      };
    }
  }
}
