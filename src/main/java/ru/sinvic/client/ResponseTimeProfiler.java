package ru.sinvic.client;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class ResponseTimeProfiler {
    private final List<Duration> responsesDurations = new ArrayList<>();

    public <T> T profileTime(Supplier<T> requestSupplier) {
        Instant startTime = Instant.now();
        T response = requestSupplier.get();
        profile(startTime, response);
        return response;
    }

    private <T> void profile(Instant startTime, CompletableFuture<T> responseAsync) {
        responseAsync.thenRun(() ->
        {
            Instant endTime = Instant.now();
            responsesDurations.add(Duration.between(startTime, endTime));
        });
    }

    private <T> void profile(Instant startTime, T responseSync) {
        Instant endTime = Instant.now();
        responsesDurations.add(Duration.between(startTime, endTime));
    }

    public HttpResponseStatistics calculateStatistics() {
        long maxTime = responsesDurations.stream().map(Duration::toMillis).max(Long::compare).orElse(0L);
        long minTime = responsesDurations.stream().map(Duration::toMillis).min(Long::compare).orElse(0L);
        double averageTime = responsesDurations.stream().map(Duration::toMillis).mapToDouble(Long::doubleValue).average().orElse(0.0);
        long firstRequestTime = responsesDurations.isEmpty() ? 0L : responsesDurations.get(0).toMillis();
        return new HttpResponseStatistics(maxTime, minTime, firstRequestTime, averageTime);
    }
}
