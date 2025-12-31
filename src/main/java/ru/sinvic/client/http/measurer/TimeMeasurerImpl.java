package ru.sinvic.client.http.measurer;

import java.time.Duration;
import java.time.Instant;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Supplier;

public class TimeMeasurerImpl implements TimeMeasurer, MeasuredTimeStatisticsCalculator {
    private final Queue<Duration> durations = new ConcurrentLinkedDeque<>();

    public <T> T measureTime(Supplier<T> taskToMeasure) {
        Instant startTime = Instant.now();
        T taskResult = taskToMeasure.get();
        if (taskResult instanceof CompletableFuture<?> taskResultFuture) {
            finishMeasurementLater(startTime, taskResultFuture);
        } else {
            finishMeasurement(startTime);
        }
        return taskResult;
    }

    public MeasuredTimeStatistics calculateStatistics() {
        long maxTime = durations.stream().map(Duration::toMillis).max(Long::compare).orElse(0L);
        long minTime = durations.stream().map(Duration::toMillis).min(Long::compare).orElse(0L);
        double averageTime = durations.stream().map(Duration::toMillis).mapToDouble(Long::doubleValue).average().orElse(0.0);
        return new MeasuredTimeStatistics(maxTime, minTime, averageTime);
    }

    private void finishMeasurementLater(Instant startTime, CompletableFuture<?> taskResultFuture) {
        taskResultFuture.thenRun(() -> finishMeasurement(startTime));
    }

    private void finishMeasurement(Instant startTime) {
        Instant endTime = Instant.now();
        durations.add(Duration.between(startTime, endTime));
    }
}
