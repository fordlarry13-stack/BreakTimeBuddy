package com.breaktimebuddy;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Default implementation of {@link GroqHttpClient} using {@link HttpClient}.
 */
public class DefaultGroqHttpClient implements GroqHttpClient {
  private final HttpClient httpClient;

  /** Creates a client with a 10-second connection timeout. */
  public DefaultGroqHttpClient() {
    this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
  }

  @Override
  public CompletableFuture<HttpResponse<String>> send(HttpRequest request) {
    return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
  }
}
