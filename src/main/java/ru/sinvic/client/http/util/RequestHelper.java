package ru.sinvic.client.http.util;

import java.net.URI;
import java.net.http.HttpRequest;
import java.time.Duration;

public class RequestHelper {
    public static HttpRequest buildRequest(String host, int port, String route) {
        return HttpRequest.newBuilder()
            .uri(URI.create(String.format("http://%s:%d/%s", host, port, route)))
            .GET()
            .timeout(Duration.ofSeconds(10))
            .build();
    }
}
