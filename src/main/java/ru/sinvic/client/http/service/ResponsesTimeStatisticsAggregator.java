package ru.sinvic.client.http.service;

import ru.sinvic.client.http.HttpResponseStatistics;

public interface ResponsesTimeStatisticsAggregator {
    HttpResponseStatistics calculateStatistics();
}
