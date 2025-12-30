package ru.sinvic.client.http;

import ru.sinvic.client.http.service.ResponseTimeMeasurer;
import ru.sinvic.client.http.service.ResponsesTimeStatisticsAggregator;

import java.time.Duration;
import java.time.Instant;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Supplier;

public class ResponseTimeHandler implements ResponseTimeMeasurer, ResponsesTimeStatisticsAggregator {
    private final Queue<Duration> responsesDurations = new ConcurrentLinkedDeque<>();

    public <T> T measureTime(Supplier<T> requestSupplier) {
        Instant startTime = Instant.now();
        T response = requestSupplier.get();
        if (response instanceof CompletableFuture<?> responseFuture) {
            profile(startTime, responseFuture);
        } else {
            profile(startTime);
        }
        return response;
    }

    private void profile(Instant startTime, CompletableFuture<?> responseAsync) {
        responseAsync.thenRun(() ->
        {
            Instant endTime = Instant.now();
            responsesDurations.add(Duration.between(startTime, endTime));
        });
    }

    private void profile(Instant startTime) {
        Instant endTime = Instant.now();
        responsesDurations.add(Duration.between(startTime, endTime));
    }

    public HttpResponseStatistics calculateStatistics() {
        long maxTime = responsesDurations.stream().map(Duration::toMillis).max(Long::compare).orElse(0L);
        long minTime = responsesDurations.stream().map(Duration::toMillis).min(Long::compare).orElse(0L);
        double averageTime = responsesDurations.stream().map(Duration::toMillis).mapToDouble(Long::doubleValue).average().orElse(0.0);
        long firstRequestTime = responsesDurations.isEmpty() ? 0L : responsesDurations.peek().toMillis();
        return new HttpResponseStatistics(maxTime, minTime, firstRequestTime, averageTime);
    }
}
