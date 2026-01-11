package ru.sinvic.client.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static final String HOST = "localhost";
    private static final int PORT = 80;

    private static final String COMMAND_START = "s";
    private static final String COMMAND_END = "e";

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        TimeMeasurerImpl timeMeasurer = new TimeMeasurerImpl();
        SimpleHttpClient simpleHttpClient = new SimpleHttpClient(100, true, HttpClient.Version.HTTP_2, timeMeasurer, executorService);

        Scanner scanner = new Scanner(System.in);
        logger.info("Ожидание команды... (введите '" + COMMAND_START + "' для запуска или '" + COMMAND_END + "' для завершения)");

        while (true) {
            String command = scanner.nextLine().trim();

            if (COMMAND_START.equalsIgnoreCase(command)) {
                logger.info("Старт выполнения...");

                try {
                    executeHttpRequests(simpleHttpClient, timeMeasurer);
                } catch (Exception e) {
                    logger.error("Ошибка при выполнении: {}", e.getMessage());
                }

                System.out.println("Выполнение завершено. Ожидание следующей команды...");
            } else if (COMMAND_END.equalsIgnoreCase(command)) {
                logger.info("Завершение работы...");
                scanner.close();
                executorService.shutdown();
                break;
            } else {
                logger.info("Неизвестная команда. Введите '" + COMMAND_START + "' или '" + COMMAND_END + "'");
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

        Optional<String> serverStatistics = simpleHttpClient.sendGetRequestSync(buildRequest(HOST, PORT, "get-statistics?path=/ping"));
        printServerStatistics(serverStatistics);
    }
}
