package ru.sinvic.client.http.scheduled;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sinvic.client.http.SimpleHttpClient;
import ru.sinvic.client.http.measurer.TimeMeasurerImpl;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.Optional;
import java.util.concurrent.*;

import static ru.sinvic.client.http.util.RequestHelper.buildRequest;
import static ru.sinvic.client.http.util.StatisticsPrinter.printServerStatistics;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static final String HOST = "176.57.217.218";
    private static final String ROUTE = "ping";
    private static final int PORT = 8080;

    public static void main(String[] args) throws InterruptedException, ExecutionException {

        TimeMeasurerImpl timeMeasurer = new TimeMeasurerImpl();
        ExecutorService executorServiceForHttpClient = Executors.newFixedThreadPool(1);
        SimpleHttpClient simpleHttpClient = new SimpleHttpClient(500, true, HttpClient.Version.HTTP_1_1, timeMeasurer, executorServiceForHttpClient);
        HttpRequest httpRequest = buildRequest(HOST, PORT, ROUTE);

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        ScheduledFuture<?> scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(() -> simpleHttpClient.sendRequestsRepeated(httpRequest), 1, 1, TimeUnit.SECONDS);

        Thread.sleep(1000 * 10 + 100);
        scheduledFuture.cancel(true);
        Thread.sleep(500);

        logger.info(timeMeasurer.calculateStatistics().toString());
        Optional<String> serverStatistics = simpleHttpClient.sendGetRequestSync(buildRequest(HOST, PORT, "get-statistics?path=/" + ROUTE));
        printServerStatistics(serverStatistics);

        executorServiceForHttpClient.shutdown();
        scheduledExecutorService.shutdown();
    }
}
