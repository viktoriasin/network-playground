package ru.sinvic.server;

import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RequestTimeInMemoryStorage {

    private final Map<String, List<Long>> requestTime = new HashMap<>();


    public void addDuration(String uriPath, Duration duration) {
        long millis = duration.toMillis();
        requestTime.computeIfAbsent(uriPath, name -> new ArrayList<>()).add(millis);
    }

    public void deleteMethodStatistics(String methodName) {
        requestTime.remove(methodName);
    }

    public RequestTimeStatisticsResult calculateRequestStatistic(@NonNull String uriPath) {
        List<Long> requestTimes = requestTime.get(uriPath);
        if (requestTimes == null) {
            return new RequestTimeStatisticsResult(null, new IllegalArgumentException("There is no information for this uri path: " + uriPath));
        }

        long maxTime = requestTimes.stream().max(Long::compareTo).orElse(0L);
        long minTime = requestTimes.stream().min(Long::compareTo).orElse(0L);
        double averageTime = requestTimes.stream().mapToDouble(Long::doubleValue).average().orElse(0.0);

        return new RequestTimeStatisticsResult(new RequestTimeStatistics(maxTime, minTime, averageTime), null);
    }

    @Override
    public String toString() {
        return requestTime.toString();
    }

    public record RequestTimeStatistics(long maxTime, long minTime, double averageTime) {
    }

    public record RequestTimeStatisticsResult(RequestTimeStatistics requestTimeStatistics, Exception error) {
    }
}
