package ru.sinvic.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.net.http.HttpResponse.BodyHandlers.ofString;

public class SimpleHttp2Client {

    private final static int ITERATIONS = 100;

    public static void main(String[] args) {
        List<Duration> durations = new ArrayList<>();

        List<CompletableFuture<Void>> requestFutures = new ArrayList<>();

        // Будем слать запросы асинхронно, чтобы можно было более полно использовать мультиплексирование в HTTP2
        HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();
        HttpRequest request = buildRequest();

        for (int i = 0; i < ITERATIONS; i++) {
            sendAsyncRequest(httpClient, request, requestFutures, durations);
        }

        CompletableFuture<Void> listCompletableFuture = CompletableFuture.allOf(requestFutures.toArray(new CompletableFuture[0]));


        listCompletableFuture
            .thenRun(() -> {
                HttpResponseStatistics httpResponseStatistics = calculateStatistics(durations);
                printStatistics(httpResponseStatistics);
                System.out.println("Работа http2 завершилась");
            }).join();
    }

    private static HttpRequest buildRequest() {
        return HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:8080/ping"))
            .GET()
            .timeout(Duration.ofSeconds(10))
            .build();
    }

    private static void sendAsyncRequest(HttpClient httpClient, HttpRequest request, List<CompletableFuture<Void>> requestFutures, List<Duration> durations) {
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

    private static HttpResponseStatistics calculateStatistics(List<Duration> responseDurations) {
        long maxTime = responseDurations.stream().map(Duration::toMillis).max(Long::compare).orElse(0L);
        long minTime = responseDurations.stream().map(Duration::toMillis).min(Long::compare).orElse(0L);
        double averageTime = responseDurations.stream().map(Duration::toMillis).mapToDouble(Long::doubleValue).average().orElse(0.0);

        return new HttpResponseStatistics(maxTime, minTime, averageTime);
    }

    private static void printStatistics(HttpResponseStatistics responseStatistics) {
        System.out.println("Среднее время ответа в миллисекундах " + responseStatistics.averageTime());
        System.out.println("Максимальное время ответа в миллисекундах " + responseStatistics.maxTime());
        System.out.println("Минимальное время ответа в миллисекундах " + responseStatistics.minTime());
    }

    private record HttpResponseStatistics(long maxTime, long minTime, double averageTime) {
    }
}
