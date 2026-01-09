package ru.sinvic.client.http.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sinvic.client.http.measurer.MeasuredTimeStatistics;

import java.util.Optional;

public class StatisticsPrinter {
    private static final Logger logger = LoggerFactory.getLogger("StatisticsPrinter");

    public static void printResponseTimeStatistics(MeasuredTimeStatistics statistics, long totalTimeMs) {
        System.out.println("Всего времени было затрачено в миллисекундах: " + totalTimeMs);
        System.out.println("Среднее время ответа в миллисекундах " + statistics.averageTime() +
            "\nМаксимальное время ответа в миллисекундах " + statistics.maxTime() +
            "\nМинимальное время ответа в миллисекундах " + statistics.minTime());
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static void printServerStatistics(Optional<String> serverStatistics) {
        System.out.println("Статистика с сервера:");
        System.out.println(serverStatistics.orElse("Не удалось собрать статистику с сервера."));
    }
}
