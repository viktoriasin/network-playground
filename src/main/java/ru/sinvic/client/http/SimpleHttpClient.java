package ru.sinvic.client.http;

import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sinvic.client.http.measurer.TimeMeasurer;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import static java.net.http.HttpResponse.BodyHandlers.ofString;

public class SimpleHttpClient {
    private static final Logger logger = LoggerFactory.getLogger(SimpleHttpClient.class);

    private final int requestsCount;
    private final boolean isAsyncRequest;
    private final HttpClient httpClient;
    private final TimeMeasurer timeMeasurer;

    // TODO: Move requestsCount & isAsyncRequest to method args
    public SimpleHttpClient(int requestsCount, boolean isAsyncRequest, @NonNull HttpClient.Version httpVersion, TimeMeasurer timeMeasurer, ExecutorService executorService) {
        this.requestsCount = requestsCount;
        this.isAsyncRequest = isAsyncRequest;
        this.timeMeasurer = timeMeasurer;
        this.httpClient = HttpClient.newBuilder()
            .version(httpVersion)
            .executor(executorService)
            .build();
    }

    public Optional<String> sendGetRequestSync(@NonNull HttpRequest request) {
        try {
            return Optional.of(httpClient.send(request, ofString()).body());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return Optional.empty();
    }

    public CompletableFuture<HttpResponse<String>> sendGetRequestAsync(@NonNull HttpRequest getRequest) {
        return httpClient.sendAsync(getRequest, ofString()).exceptionally(ex -> {
            logger.error("Ошибка при выполнении асинхронного http post запроса {}", ex.getMessage());
            return null;
        });
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
            CompletableFuture<?> httpResponseCompletableFuture = sendAsyncMeasured(request);
            if (httpResponseCompletableFuture != null) {
                responsesFuture.add(httpResponseCompletableFuture);
            }
        }
        CompletableFuture<Void> voidCompletableFuture = CompletableFuture.allOf(responsesFuture.toArray(new CompletableFuture[0]));
        voidCompletableFuture
            .thenRun(() -> logger.info("Работа async http завершилась. Было отправлено {} запросов асинхронно.", requestsCount))
            .join();
    }

    private void sendSyncRepeated(HttpRequest request) {
        for (int i = 0; i < requestsCount; i++) {
            sendSyncMeasured(request);
        }
        logger.info("Работа sync http завершилась Было отправлено {} запросов асинхронно.", requestsCount);
    }

    private CompletableFuture<?> sendAsyncMeasured(HttpRequest request) {
        return timeMeasurer.measureTime(() -> httpClient.sendAsync(request, ofString()))
            .exceptionally(ex -> {
                logger.error("Ошибка при выполнении асинхронного http запроса {}", ex.getMessage());
                return null;
            });
    }

    private void sendSyncMeasured(HttpRequest request) {
        timeMeasurer.measureTime(() -> {
            try {
                return Optional.of(httpClient.send(request, ofString()));
            } catch (HttpTimeoutException e) {
                logger.error("ERROR: Request timed out");
            } catch (IOException | InterruptedException e) {
                logger.error("ERROR: {}", e.getMessage());
            }
            return Optional.empty();
        });
    }
}

