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

import static java.net.http.HttpResponse.BodyHandlers.ofString;

public class SimpleHttp1Client {

    private final static int ITERATIONS = 100;

    public static void main(String[] args) {
        List<Duration> durations = new ArrayList<>();

        // за счет пула соединений, который в HttpClient создается, при запросе к одному хосту даже в HTTP1 расходы на установку нового соединения сведены к минимуму
        HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();
        HttpRequest request = buildRequest();

        for (int i = 0; i < ITERATIONS; i++) {
            sendRequestAndSaveResponseTime(httpClient, request, durations);
        }

        HttpResponseStatistics responseStatistics = calculateStatistics(durations);

        printStatistics(responseStatistics);
    }

    private static HttpRequest buildRequest() {
        return HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:8080/ping"))
            .GET()
            .timeout(Duration.ofSeconds(10))
            .build();
    }

    private static void sendRequestAndSaveResponseTime(java.net.http.HttpClient httpClient, HttpRequest request, List<Duration> durations) {
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
