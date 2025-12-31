package ru.sinvic.client.http.measurer;

import java.util.function.Supplier;

public interface TimeMeasurer {
    <T> T measureTime(Supplier<T> taskToMeasure);
}
