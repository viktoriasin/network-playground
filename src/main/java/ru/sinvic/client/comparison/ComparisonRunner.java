package ru.sinvic.client.comparison;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;

public class ComparisonRunner {

    private static final Logger logger = LoggerFactory.getLogger(ComparisonRunner.class.getSimpleName());

    private static final int REQUEST_COUNT = 10;

    public static void main(String[] args) {
        logger.info("Launch ComparisonRunner with {} requests", REQUEST_COUNT);

        performRequestsAndLogTime("HTTP", () -> new ComparisonClientHttp().sendRequests(REQUEST_COUNT));
        performRequestsAndLogTime("GRPC", () -> new ComparisonClientGRPC().sendRequests(REQUEST_COUNT));
    }

    static void performRequestsAndLogTime(String prefix, Runnable requestsPerformer) {
        Instant startTime = Instant.now();
        requestsPerformer.run();
        Instant endTime = Instant.now();
        long totalTimeMs = Duration.between(startTime, endTime).toMillis();
        logger.info("[{}] Total time is {}", prefix, totalTimeMs);
    }
}
