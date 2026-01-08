package ru.sinvic.client.http;

import ru.sinvic.client.http.measurer.TimeMeasurerImpl;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ru.sinvic.client.http.util.RequestHelper.buildRequest;
import static ru.sinvic.client.http.util.StatisticsPrinter.printResponseTimeStatistics;
import static ru.sinvic.client.http.util.StatisticsPrinter.printServerStatistics;

public class Main {
    private static final String HOST = "localhost"; // localhost
    private static final int PORT = 8080;

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        TimeMeasurerImpl timeMeasurer = new TimeMeasurerImpl();
        SimpleHttpClient simpleHttpClient = new SimpleHttpClient(100, true, HttpClient.Version.HTTP_2, timeMeasurer, executorService);

        Scanner scanner = new Scanner(System.in);
        System.out.println("Ожидание команды... (введите 's' для запуска или 'e' для завершения)");

        while (true) {
            String command = scanner.nextLine().trim();

            if ("s".equalsIgnoreCase(command)) {
                System.out.println("Старт выполнения...");

                try {
                    executeHttpRequests(simpleHttpClient, timeMeasurer);
                } catch (Exception e) {
                    System.err.println("Ошибка при выполнении: " + e.getMessage());
                }

                System.out.println("Выполнение завершено. Ожидание следующей команды...");
            } else if ("e".equalsIgnoreCase(command)) {
                System.out.println("Завершение работы...");
                scanner.close();
                executorService.shutdown();
                break;
            } else {
                System.out.println("Неизвестная команда. Введите 'start' или 'end work session'.");
            }
        }
    }

    public static void executeHttpRequests(SimpleHttpClient simpleHttpClient, TimeMeasurerImpl timeMeasurer) throws InterruptedException {
        HttpRequest request = buildRequest(HOST, PORT, "ping");

        Instant startAllTime = Instant.now();
        simpleHttpClient.sendRequestsRepeated(request);
        Instant endAllTime = Instant.now();

        long totalTimeMs = Duration.between(startAllTime, endAllTime).toMillis();
        printResponseTimeStatistics(timeMeasurer.calculateStatistics(), totalTimeMs);

        Optional<String> serverStatistics = simpleHttpClient.sendRequest(buildRequest(HOST, PORT, "get-statistics?path=/ping"));
        printServerStatistics(serverStatistics);
    }
}
