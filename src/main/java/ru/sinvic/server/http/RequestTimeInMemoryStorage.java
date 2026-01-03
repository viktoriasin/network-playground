package ru.sinvic.server.http;

import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class RequestTimeInMemoryStorage {

    private final Map<String, Queue<Long>> requestTime = new ConcurrentHashMap<>();

    public void addDuration(String uriPath, Duration duration) {
        long millis = duration.toMillis();
        requestTime.computeIfAbsent(uriPath, name -> new ConcurrentLinkedQueue<>()).add(millis);
    }

    public RequestTimeStatisticsResult calculateRequestStatistic(@NonNull String uriPath) {
        Queue<Long> requestTimes = requestTime.get(uriPath);
        if (requestTimes == null || requestTimes.isEmpty()) {
            return new RequestTimeStatisticsResult(null, "There is no information for this uri path: " + uriPath);
        }

        long maxTime = requestTimes.stream().max(Long::compareTo).orElse(0L);
        long minTime = requestTimes.stream().min(Long::compareTo).orElse(0L);
        double averageTime = requestTimes.stream().mapToDouble(Long::doubleValue).average().orElse(0.0);

        return new RequestTimeStatisticsResult(new RequestTimeStatistics(maxTime, minTime, averageTime), null);
    }

    public record RequestTimeStatistics(long maxTime, long minTime, double averageTime) {
    }

    public record RequestTimeStatisticsResult(RequestTimeStatistics requestTimeStatistics, String fallbackMessage) {
    }
}
