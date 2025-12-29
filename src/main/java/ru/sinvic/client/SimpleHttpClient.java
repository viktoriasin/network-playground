package ru.sinvic.client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.net.http.HttpResponse.BodyHandlers.ofString;

public class SimpleHttpClient {

    private static final int REQUESTS_NUMBER = 100;
    private static final boolean IS_ASYNC_REQUEST = true;

    private final HttpClient.Version HTTP_VERSION = HttpClient.Version.HTTP_1_1;

    private final HttpClient httpClient;
    private final ResponseTimeProfiler responseTimeProfiler;

    public SimpleHttpClient(ResponseTimeProfiler responseTimeProfiler) {
        this.responseTimeProfiler = responseTimeProfiler;
        this.httpClient = HttpClient.newBuilder()
            .version(HTTP_VERSION)
            .build();
    }

    public static void main(String[] args) {

        HttpRequest request = buildRequest();

        ResponseTimeProfiler responseTimeProfiler = new ResponseTimeProfiler();
        SimpleHttpClient simpleHttpClient = new SimpleHttpClient(responseTimeProfiler);

        Instant startAllTime = Instant.now();
        simpleHttpClient.sendRequestsInIterations(request);
        Instant endAllTime = Instant.now();

        System.out.println("Всего времени было затрачено в миллисекундах: " + Duration.between(startAllTime, endAllTime).toMillis());
        System.out.println(responseTimeProfiler.calculateStatistics());
    }

    private static HttpRequest buildRequest() {
        return HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:8080/ping"))
            .GET()
            .timeout(Duration.ofSeconds(10))
            .build();
    }

    private void sendRequestsInIterations(HttpRequest request) {
        if (IS_ASYNC_REQUEST) {
            sendAsyncInIterations(request);
        } else {
            sendSyncInIterations(request);
        }
    }

    private void sendAsyncInIterations(HttpRequest request) {
        List<CompletableFuture<HttpResponse<String>>> responsesFuture = new ArrayList<>();
        for (int i = 0; i < REQUESTS_NUMBER; i++) {
            CompletableFuture<HttpResponse<String>> httpResponseCompletableFuture = sendAsyncWithProfile(request);
            responsesFuture.add(httpResponseCompletableFuture);
        }
        CompletableFuture<Void> voidCompletableFuture = CompletableFuture.allOf(responsesFuture.toArray(new CompletableFuture[0]));
        voidCompletableFuture
            .thenRun(() -> {
                System.out.println("Работа async http завершилась");
            });
    }

    private void sendSyncInIterations(HttpRequest request) {
        for (int i = 0; i < REQUESTS_NUMBER; i++) {
            HttpResponse<String> stringHttpResponse = sendSyncWithProfile(request);
        }
        System.out.println("Работа sync http завершилась");
    }

    private CompletableFuture<HttpResponse<String>> sendAsyncWithProfile(HttpRequest request) {
        return responseTimeProfiler.profileTime(() -> httpClient.sendAsync(request, ofString()));
    }

    private HttpResponse<String> sendSyncWithProfile(HttpRequest request) {
        return responseTimeProfiler.profileTime(() -> {
            try {
                return httpClient.send(request, ofString());
            } catch (HttpTimeoutException e) {
                System.err.println("ERROR: Request timed out");
            } catch (IOException | InterruptedException e) {
                System.err.println("ERROR: " + e.getMessage());
            }
            return null;
        });
    }
}

