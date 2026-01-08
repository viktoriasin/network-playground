package ru.sinvic.client.http;

import lombok.NonNull;
import ru.sinvic.client.http.measurer.TimeMeasurer;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import static java.net.http.HttpResponse.BodyHandlers.ofString;

public class SimpleHttpClient {

    private final int requestsCount;
    private final boolean isAsyncRequest;
    private final HttpClient httpClient;
    private final TimeMeasurer timeMeasurer;

    public SimpleHttpClient(int requestsCount, boolean isAsyncRequest, @NonNull HttpClient.Version httpVersion, TimeMeasurer timeMeasurer, ExecutorService executorService) {
        this.requestsCount = requestsCount;
        this.isAsyncRequest = isAsyncRequest;
        this.timeMeasurer = timeMeasurer;
        this.httpClient = HttpClient.newBuilder()
            .version(httpVersion)
            .executor(executorService)
            .build();
    }

    public Optional<String> sendRequest(@NonNull HttpRequest request) {
        try {
            return Optional.of(httpClient.send(request, ofString()).body());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return Optional.empty();
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
            if (httpResponseCompletableFuture != null) {
                responsesFuture.add(httpResponseCompletableFuture);
            }
        }
        CompletableFuture<Void> voidCompletableFuture = CompletableFuture.allOf(responsesFuture.toArray(new CompletableFuture[0]));
        voidCompletableFuture
            .thenRun(() -> System.out.println("Работа async http завершилась. Было отправлено " + requestsCount + " запросов асинхронно."))
            .join();
    }

    private void sendSyncRepeated(HttpRequest request) {
        for (int i = 0; i < requestsCount; i++) {
            sendSyncWithProfile(request);
        }
        System.out.println("Работа sync http завершилась Было отправлено " + requestsCount + " запросов асинхронно.");
    }

    private CompletableFuture<?> sendAsyncWithProfile(HttpRequest request) {
        return timeMeasurer.measureTime(() -> httpClient.sendAsync(request, ofString()))
            .exceptionally(ex -> {
                System.out.println("Ошибка при выполнении асинхронного http запроса " + ex.getMessage());
                return null;
            });
    }

    private void sendSyncWithProfile(HttpRequest request) {
        timeMeasurer.measureTime(() -> {
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

