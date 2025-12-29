package ru.sinvic.client;

public record HttpResponseStatistics(long maxTime, long minTime, long firstRequestTime, double averageTime) {
    @Override
    public String toString() {
        return "Среднее время ответа в миллисекундах " + averageTime +
            "\nМаксимальное время ответа в миллисекундах " + maxTime +
            "\nМинимальное время ответа в миллисекундах " + minTime +
            "\nВремя первого запроса в миллисекундах " + firstRequestTime;
    }
}
