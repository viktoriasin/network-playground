package ru.sinvic.server.http.perf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.sinvic.client.socket.nio.NIOClientSocket;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.IntStream;

@Component
public class LeakedContainer {
    private static final Logger logger = LoggerFactory.getLogger(LeakedContainer.class);
    private final ConcurrentLinkedQueue<List<Object>> storage = new ConcurrentLinkedQueue<>();

    public void addObjects() {
        logger.info("add objects");
        storage.add(createObjects());
    }

    public void clear() {
        logger.info("clear container");
        storage.clear();
    }

    private List<Object> createObjects() {
        return IntStream.range(0, 100).mapToObj(i -> new Object()).toList();
    }
}
