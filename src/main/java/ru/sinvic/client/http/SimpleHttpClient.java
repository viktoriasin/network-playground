package ru.sinvic.client.http;

import lombok.NonNull;
import ru.sinvic.client.http.service.ResponseTimeMeasurer;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static java.net.http.HttpResponse.BodyHandlers.ofString;

public class SimpleHttpClient {

    private final int requestsCount;
    private final boolean isAsyncRequest;
    private final HttpClient.Version httpVersion;
    private final HttpClient httpClient;
    private final ResponseTimeMeasurer responseTimeMeasurer;

    public SimpleHttpClient(int requestsCount, boolean isAsyncRequest, @NonNull HttpClient.Version httpVersion, @NonNull ResponseTimeMeasurer responseTimeMeasurer) {
        this.requestsCount = requestsCount;
        this.isAsyncRequest = isAsyncRequest;
        this.httpVersion = httpVersion;
        this.responseTimeMeasurer = responseTimeMeasurer;
        this.httpClient = HttpClient.newBuilder()
            .version(this.httpVersion)
            .build();
    }

    public void sendRequestsRepeated(@NonNull HttpRequest request) {
        if (isAsyncRequest) {
            sendAsyncRepeated(request);
        } else {
            sendSyncRepeated(request);
        }
    }

    private void sendAsyncRepeated(HttpRequest request) {
        List<CompletableFuture<?>> responsesFuture = new ArrayList<>();
        for (int i = 0; i < requestsCount; i++) {
            CompletableFuture<?> httpResponseCompletableFuture = sendAsyncWithProfile(request);
            responsesFuture.add(httpResponseCompletableFuture);
        }
        CompletableFuture<Void> voidCompletableFuture = CompletableFuture.allOf(responsesFuture.toArray(new CompletableFuture[0]));
        voidCompletableFuture
            .thenRun(() -> System.out.println("Работа async http завершилась"))
            .join();
    }

    private void sendSyncRepeated(HttpRequest request) {
        for (int i = 0; i < requestsCount; i++) {
            sendSyncWithProfile(request);
        }
        System.out.println("Работа sync http завершилась");
    }

    private CompletableFuture<?> sendAsyncWithProfile(HttpRequest request) {
        return responseTimeMeasurer.measureTime(() -> httpClient.sendAsync(request, ofString()));
    }

    private void sendSyncWithProfile(HttpRequest request) {
        responseTimeMeasurer.measureTime(() -> {
            try {
                return Optional.of(httpClient.send(request, ofString()));
            } catch (HttpTimeoutException e) {
                System.err.println("ERROR: Request timed out");
            } catch (IOException | InterruptedException e) {
                System.err.println("ERROR: " + e.getMessage());
            }
            return Optional.empty();
        });
    }
}

