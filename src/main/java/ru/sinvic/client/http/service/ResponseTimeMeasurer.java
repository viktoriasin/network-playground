package ru.sinvic.client.http.service;

import java.util.function.Supplier;

public interface ResponseTimeMeasurer {
    <T> T measureTime(Supplier<T> requestSupplier);
}
