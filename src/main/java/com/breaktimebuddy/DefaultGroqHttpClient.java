package com.breaktimebuddy;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class DefaultGroqHttpClient implements GroqHttpClient {

    private final HttpClient httpClient;

    public DefaultGroqHttpClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public CompletableFuture<HttpResponse<String>> send(
            HttpRequest request
    ) {
        return httpClient.sendAsync(
                request,
                HttpResponse.BodyHandlers.ofString()
        );
    }
}
