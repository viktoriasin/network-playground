package ru.sinvic.client.http;

import ru.sinvic.client.http.measurer.MeasuredTimeStatistics;
import ru.sinvic.client.http.measurer.TimeMeasurerImpl;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        TimeMeasurerImpl timeMeasurer = new TimeMeasurerImpl();
        SimpleHttpClient simpleHttpClient = new SimpleHttpClient(100, true, HttpClient.Version.HTTP_2, timeMeasurer, executorService);

        Scanner scanner = new Scanner(System.in);
        System.out.println("Ожидание команды... (введите 'start' для запуска или 'end' для завершения)");

        while (true) {
            String command = scanner.nextLine().trim();

            if ("start".equalsIgnoreCase(command)) {
                System.out.println("Старт выполнения...");

                try {
                    executeHttpRequests(simpleHttpClient, timeMeasurer);
                } catch (Exception e) {
                    System.err.println("Ошибка при выполнении: " + e.getMessage());
                    e.printStackTrace();
                }

                System.out.println("Выполнение завершено. Ожидание следующей команды...");
            } else if ("end".equalsIgnoreCase(command)) {
                System.out.println("Завершение работы...");
                scanner.close();
                break;
            } else {
                System.out.println("Неизвестная команда. Введите 'start' или 'end work session'.");
            }
        }
    }

    public static void executeHttpRequests(SimpleHttpClient simpleHttpClient, TimeMeasurerImpl timeMeasurer) throws InterruptedException {
        HttpRequest request = buildRequest("ping");

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
