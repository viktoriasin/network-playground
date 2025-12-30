package ru.sinvic.client.http;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.time.Instant;

public class Main {
    public static void main(String[] args) {
        HttpRequest request = buildRequest();

        ResponseTimeHandler responseTimeHandler = new ResponseTimeHandler();
        SimpleHttpClient simpleHttpClient = new SimpleHttpClient(100, true, HttpClient.Version.HTTP_1_1, responseTimeHandler);

        Instant startAllTime = Instant.now();
        simpleHttpClient.sendRequestsRepeated(request);
        Instant endAllTime = Instant.now();

        System.out.println("Всего времени было затрачено в миллисекундах: " + Duration.between(startAllTime, endAllTime).toMillis());
        System.out.println(responseTimeHandler.calculateStatistics());
    }

    private static HttpRequest buildRequest() {
        return HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:8080/ping"))
            .GET()
            .timeout(Duration.ofSeconds(10))
            .build();
    }
}
