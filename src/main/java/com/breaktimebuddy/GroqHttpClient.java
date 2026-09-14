package com.breaktimebuddy;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public interface GroqHttpClient {
    CompletableFuture<HttpResponse<String>> send(HttpRequest request);
}
