package ru.sinvic.client.http;

import ru.sinvic.client.http.measurer.MeasuredTimeStatistics;
import ru.sinvic.client.http.measurer.TimeMeasurerImpl;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        HttpRequest request = buildRequest("ping");

        TimeMeasurerImpl timeMeasurer = new TimeMeasurerImpl();
        SimpleHttpClient simpleHttpClient = new SimpleHttpClient(100, true, HttpClient.Version.HTTP_1_1, timeMeasurer);

        Instant startAllTime = Instant.now();
        simpleHttpClient.sendRequestsRepeated(request);
        Instant endAllTime = Instant.now();

        long totalTimeMs = Duration.between(startAllTime, endAllTime).toMillis();
        printResponseTimeStatistics(timeMeasurer.calculateStatistics(), totalTimeMs);

        Optional<String> serverStatistics = simpleHttpClient.sendRequest(buildRequest("get-statistics?path=/ping"));
        printServerStatistics(serverStatistics);
    }

    private static HttpRequest buildRequest(String route) {
        return HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:8080/" + route))
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

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static void printServerStatistics(Optional<String> serverStatistics) {
        System.out.println("Статистика с сервера:");
        System.out.println(serverStatistics.orElse("Не удалось собрать статистику с сервера."));
    }
}
