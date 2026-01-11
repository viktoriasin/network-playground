package ru.sinvic.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sinvic.client.grpc.GRPCClient;
import ru.sinvic.client.http.SimpleHttpClient;
import ru.sinvic.client.http.measurer.TimeMeasurerImpl;
import ru.sinvic.client.http.util.RequestHelper;
import ru.sinvic.client.util.Config;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.time.Instant;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommonClient {
    private static final Logger logger = LoggerFactory.getLogger("CommonClient");

    private static final String CONFIG_FINALE_NAME = "/clients_config.properties";
    private static final ExecutorService executorService = Executors.newFixedThreadPool(10);
    private static String grpcHost;
    private static int grpcPort;
    private static String httpHost;
    private static int requestCount;
    private static int httpPort;

    public CommonClient() {
        configureClient();
    }

    public static void main(String[] args) throws InterruptedException {
        CommonClient commonClient = new CommonClient();


        Instant startTime = Instant.now();
        commonClient.makeGrpcRequests();
        Instant endTime = Instant.now();
        long totalTimeMs = Duration.between(startTime, endTime).toMillis();
        logger.info("Total time for {} for grpc client is {}", requestCount, totalTimeMs);

        Instant startHttpTime = Instant.now();
        makeHttpRequests();
        Instant endHttpTime = Instant.now();
        long totalHttpTimeMs = Duration.between(startHttpTime, endHttpTime).toMillis();
        logger.info("Total time for {} for http client is {}", requestCount, totalHttpTimeMs);

        executorService.shutdown();
    }

    private static void makeHttpRequests() throws InterruptedException {
        SimpleHttpClient simpleHttpClient = new SimpleHttpClient(requestCount, true, HttpClient.Version.HTTP_1_1, new TimeMeasurerImpl(), executorService);
        CountDownLatch latch = new CountDownLatch(requestCount);
        for (int i = 0; i < requestCount; i++) {
            logger.info("send http request with request id {}", i);
            HttpRequest httpRequest = RequestHelper.buildRequest(httpHost, httpPort, "get-message?request_id=" + i);
            simpleHttpClient.sendGetRequestAsync(httpRequest).thenApply(response -> {
                latch.countDown();
                logger.info("recieve http server response with data: {}", response.body());
                return response;
            });
        }
        latch.await();
    }

    private void configureClient() {
        Properties properties = null;
        try {
            properties = Config.readProperties(CONFIG_FINALE_NAME);
            grpcHost = properties.getProperty("grpc.host");
            grpcPort = Integer.parseInt(properties.getProperty("grpc.port"));
            httpHost = properties.getProperty("http.host");
            httpPort = Integer.parseInt(properties.getProperty("http.port"));
            requestCount = Integer.parseInt(properties.getProperty("requests.count"));
        } catch (IOException e) {
            throw new IllegalArgumentException("could not parse properties file: " + CONFIG_FINALE_NAME);
        }
    }

    private void makeGrpcRequests() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(requestCount);
        GRPCClient grpcClient = new GRPCClient(grpcHost, grpcPort);
        for (int i = 0; i < requestCount; i++) {
            grpcClient.request(latch);
        }
        latch.await();
        grpcClient.shutDown();
    }
}
