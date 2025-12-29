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
import java.util.concurrent.ExecutorService;

import static java.net.http.HttpResponse.BodyHandlers.ofString;

public class SimpleHttpClient {

    private final static int REQUESTS_NUMBER = 100;

    private final HttpClient httpClient;

    public SimpleHttpClient(HttpClient.Version HTTP_VERSION) {
        this.httpClient = HttpClient.newBuilder()
            .version(HTTP_VERSION)
            .build();
    }

    public SimpleHttpClient(HttpClient.Version HTTP_VERSION, ExecutorService executorService) {
        this.httpClient = HttpClient.newBuilder()
            .version(HTTP_VERSION)
            .executor(executorService)
            .build();
        ;
    }

    public static void main(String[] args) {

        HttpRequest request = buildRequest();
        SimpleHttpClient simpleHttpClient = new SimpleHttpClient(HttpClient.Version.HTTP_1_1);

        Instant startAllTime = Instant.now();

        for (int i = 0; i < REQUESTS_NUMBER; i++) {
            simpleHttpClient.send(request, false);
        }

        Instant endAllTime = Instant.now();

        System.out.println("Всего времени: " + Duration.between(startAllTime, endAllTime).toMillis());
    }

    private static HttpRequest buildRequest() {
        return HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:8080/ping"))
            .GET()
            .timeout(Duration.ofSeconds(10))
            .build();
    }

    private void send(HttpRequest request, boolean isAsync) {
        List<Duration> durations = new ArrayList<>();
        if (isAsync) {
            List<CompletableFuture<Void>> requestFutures = new ArrayList<>();
            sendAsync(request, durations, requestFutures);
            CompletableFuture<Void> listCompletableFuture = CompletableFuture.allOf(requestFutures.toArray(new CompletableFuture[0]));
            listCompletableFuture
                .thenRun(() -> {
                    HttpResponseStatistics httpResponseStatistics = calculateStatistics(durations);
                    printStatistics(httpResponseStatistics);
                    System.out.println("Работа http2 завершилась");
                }).join();
        } else {
            sendSync(request, durations);
        }
    }

    private void sendAsync(HttpRequest request, List<Duration> durations, List<CompletableFuture<Void>> requestFutures) {
        Instant startTime = Instant.now();
        CompletableFuture<Void> voidCompletableFuture = httpClient.sendAsync(request, ofString())
            .thenRun(() ->
                {
                    Instant endTime = Instant.now();
                    durations.add(Duration.between(startTime, endTime));
                }
            );
        requestFutures.add(voidCompletableFuture);
    }

    private void sendSync(HttpRequest request, List<Duration> durations) {
        try {
            Instant startTime = Instant.now();
            HttpResponse<String> response = httpClient.send(request, ofString());
            Instant endTime = Instant.now();
            durations.add(Duration.between(startTime, endTime));
        } catch (HttpTimeoutException e) {
            System.err.println("ERROR: Request timed out");
        } catch (IOException | InterruptedException e) {
            System.err.println("ERROR: " + e.getMessage());
        }
    }

    private HttpResponseStatistics calculateStatistics(List<Duration> responseDurations) {
        long maxTime = responseDurations.stream().map(Duration::toMillis).max(Long::compare).orElse(0L);
        long minTime = responseDurations.stream().map(Duration::toMillis).min(Long::compare).orElse(0L);
        double averageTime = responseDurations.stream().map(Duration::toMillis).mapToDouble(Long::doubleValue).average().orElse(0.0);

        return new HttpResponseStatistics(maxTime, minTime, averageTime);
    }

    private void printStatistics(HttpResponseStatistics responseStatistics) {
        System.out.println("Среднее время ответа в миллисекундах " + responseStatistics.averageTime());
        System.out.println("Максимальное время ответа в миллисекундах " + responseStatistics.maxTime());
        System.out.println("Минимальное время ответа в миллисекундах " + responseStatistics.minTime());
    }

    private record HttpResponseStatistics(long maxTime, long minTime, double averageTime) {
    }
}
