package ru.sinvic.client.http;

import ru.sinvic.client.http.measurer.MeasuredTimeStatistics;
import ru.sinvic.client.http.measurer.TimeMeasurerImpl;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.time.Instant;

public class Main {
    public static void main(String[] args) {
        HttpRequest request = buildRequest();

        TimeMeasurerImpl timeMeasurer = new TimeMeasurerImpl();
        SimpleHttpClient simpleHttpClient = new SimpleHttpClient(100, true, HttpClient.Version.HTTP_1_1, timeMeasurer);

        Instant startAllTime = Instant.now();
        simpleHttpClient.sendRequestsRepeated(request);
        Instant endAllTime = Instant.now();

        long totalTimeMs = Duration.between(startAllTime, endAllTime).toMillis();
        printResponseTimeStatistics(timeMeasurer.calculateStatistics(), totalTimeMs);
    }

    private static HttpRequest buildRequest() {
        return HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:8080/ping"))
            .GET()
            .timeout(Duration.ofSeconds(10))
            .build();
    }

    private static void printResponseTimeStatistics(MeasuredTimeStatistics statistics, long totalTimeMs) {
        System.out.println("Всего времени было затрачено в миллисекундах: " + totalTimeMs);
        System.out.println("Среднее время ответа в миллисекундах " + statistics.averageTime() +
            "\nМаксимальное время ответа в миллисекундах " + statistics.maxTime() +
            "\nМинимальное время ответа в миллисекундах " + statistics.minTime());
    }
}
