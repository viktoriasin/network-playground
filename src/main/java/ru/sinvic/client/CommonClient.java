package ru.sinvic.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sinvic.client.grpc.GRPCClient;
import ru.sinvic.client.util.Config;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Properties;

public class CommonClient {
    private static final Logger logger = LoggerFactory.getLogger("CommonClient");

    private static final String CONFIG_FINALE_NAME = "/clients_config.properties";
    private static String host;
    private static int port;
    private static int requestCount;

    public CommonClient() {
        configureClient();
    }

    public static void main(String[] args) throws InterruptedException {
        CommonClient commonClient = new CommonClient();
        Instant startTime = Instant.now();
        commonClient.makeGrpcRequests();
        Instant endTime = Instant.now();
        long totalTimeMs = Duration.between(startTime, endTime).toMillis();

        logger.info("Total time for {} for common client is {}", requestCount, totalTimeMs);
    }

    private void configureClient() {
        Properties properties = null;
        try {
            properties = Config.readProperties(CONFIG_FINALE_NAME);
            host = properties.getProperty("host");
            port = Integer.parseInt(properties.getProperty("port"));
            requestCount = Integer.parseInt(properties.getProperty("requests.count"));
        } catch (IOException e) {
            throw new IllegalArgumentException("could not parse properties file: " + CONFIG_FINALE_NAME);
        }
    }

    private void makeGrpcRequests() throws InterruptedException {
        GRPCClient grpcClient = new GRPCClient(host, port);
        for (int i = 0; i < requestCount; i++) {
            grpcClient.request();
        }
        grpcClient.shutDown();
    }
}
