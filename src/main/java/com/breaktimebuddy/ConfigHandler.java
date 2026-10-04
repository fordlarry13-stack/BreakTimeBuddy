package com.breaktimebuddy;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

/**
 * Handles (de)serialization and sanitization of user configuration data as JSON from a
 * {@link Storage} instance.
 *
 * It reads from and writes to the provided {@code Storage}, using Gson to (de)serialize JSON and
 * invoking {@link ConfigData#sanitize(ConfigData)} on the deserialized object.
 *
 * This class is thread‑safe when the supplied {@code Storage} is thread‑safe.
 */

public class ConfigHandler {
  private final Storage storage;

  /**
   * Creates a new {@code ConfigHandler} that reads from and writes to the given {@link Storage}.
   *
   * @param storage the storage backend used for reading and writing configuration data
   */
  public ConfigHandler(Storage storage) {
    this.storage = storage;
  }

  /**
   * Returns the configuration data loaded from the connected storage, after deserializing it as
   * JSON and applying sanitization.
   *
   * @return a sanitized {@code ConfigData} instance deserialized from the storage
   * @throws IOException thrown when the connected {@link Storage} throws an {@code IOException}
   * @throws JsonParseException thrown when an error occurs during JSON parsing
   */
  public ConfigData read() throws IOException, JsonParseException {
    Gson gson = new Gson();
    try (InputStream in = storage.in();
        var reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
      return ConfigData.sanitize(gson.fromJson(reader, ConfigData.class));
    }
  }

  /**
   * Serializes the given configuration data to JSON and writes it to the connected storage.
   *
   * @param data the data to be saved
   * @throws IOException thrown when the connected {@link Storage} throws an {@code IOException}
   */
  public void write(ConfigData data) throws IOException {
    Gson gson = new GsonBuilder().setVersion(1.0).create();
    try (OutputStream out = storage.out();
        var writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
      gson.toJson(data, writer);
    }
  }
}
