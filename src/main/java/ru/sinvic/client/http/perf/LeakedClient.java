package ru.sinvic.client.http.perf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sinvic.server.http.perf.LeakedContainer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.net.http.HttpResponse.BodyHandlers.ofString;

public class LeakedClient {
    private static final Logger logger = LoggerFactory.getLogger(LeakedContainer.class);

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


    private static HttpRequest buildRequest(String route) {
        return HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:8080/" + route))
            .GET()
            .timeout(Duration.ofSeconds(10))
            .build();
    }

    private static Object doWorkCreate(HttpClient httpClient) throws IOException, InterruptedException {
        logger.info("sending request");
        return httpClient.send(buildRequest("create-objects"), ofString());
    }

    private static Object doWorkClear(HttpClient httpClient) throws IOException, InterruptedException {
        logger.info("sending request clear");
        return httpClient.send(buildRequest("clear-objects"), ofString());
    }
}
