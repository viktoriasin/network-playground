package ru.sinvic.client.http.perf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static ru.sinvic.client.http.util.RequestHelper.buildRequest;

public class LeakedClient {
    private static final Logger logger = LoggerFactory.getLogger(LeakedClient.class);
    private static final String HOST = "localhost";
    private static final int PORT = 8080;

    public static void main(String[] args) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();

        for (int i = 0; i < 10; i++) {
            executorService.submit(() -> LeakedClient.doWorkCreate(httpClient));
        }

        Thread.sleep(1000);

        executorService.submit(() -> LeakedClient.doWorkClear(httpClient));

        executorService.shutdown();
    }

    private static Object doWorkCreate(HttpClient httpClient) throws IOException, InterruptedException {
        logger.info("sending request");
        return httpClient.send(buildRequest(HOST, PORT, "create-objects"), ofString());
    }

    private static Object doWorkClear(HttpClient httpClient) throws IOException, InterruptedException {
        logger.info("sending request clear");
        return httpClient.send(buildRequest(HOST, PORT, "clear-objects"), ofString());
    }
}
