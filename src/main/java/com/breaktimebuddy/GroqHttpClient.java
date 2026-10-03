package com.breaktimebuddy;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

/**
 * HTTP client interface for sending requests to the Groq API.
 */
public interface GroqHttpClient {
  /**
   * Sends an HTTP request asynchronously.
   *
   * @param request the HTTP request to send
   * @return a future that completes with the HTTP response
   */
  CompletableFuture<HttpResponse<String>> send(HttpRequest request);
}
