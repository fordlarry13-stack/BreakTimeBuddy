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
 * (De)serializes user configuration data from/to JSON storage.
 */
public class ConfigHandler {
  private final Storage storage;

  public ConfigHandler(Storage storage) {
    this.storage = storage;
  }

  public ConfigData read() throws IOException, JsonParseException {
    Gson gson = new Gson();
    try (InputStream in = storage.in();
        var reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
      return gson.fromJson(reader, ConfigData.class);
    }
  }

  public void write(ConfigData data) throws IOException {
    Gson gson = new GsonBuilder().setVersion(1.0).create();
    try (OutputStream out = storage.out();
        var writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
      gson.toJson(data, writer);
    }
  }
}
