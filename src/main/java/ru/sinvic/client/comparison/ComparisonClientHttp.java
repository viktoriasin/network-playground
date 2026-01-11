package ru.sinvic.client.comparison;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sinvic.client.http.SimpleHttpClient;
import ru.sinvic.client.http.measurer.TimeMeasurerImpl;
import ru.sinvic.client.http.util.RequestHelper;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ComparisonClientHttp {

    private static final Logger logger = LoggerFactory.getLogger(ComparisonClientHttp.class.getSimpleName());

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    void sendRequests(int requestCount) {
        SimpleHttpClient simpleHttpClient = new SimpleHttpClient(requestCount, true, HttpClient.Version.HTTP_1_1, new TimeMeasurerImpl(), executorService);
        CountDownLatch latch = new CountDownLatch(requestCount);
        for (int i = 0; i < requestCount; i++) {
            logger.debug("send http request with request id {}", i);
            HttpRequest httpRequest = RequestHelper.buildRequest(ComparisonConstants.HOST_COMMON, ComparisonConstants.PORT_HTTP, "get-message?request_id=" + i);
            simpleHttpClient.sendGetRequestAsync(httpRequest).thenApply(response -> {
                latch.countDown();
                logger.debug("receive http server response with data: {}", response.body());
                return response;
            });
        }
        try {
            latch.await();
            logger.debug("CountDownLatch has reached zero");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            executorService.shutdown();
        }
    }
}
