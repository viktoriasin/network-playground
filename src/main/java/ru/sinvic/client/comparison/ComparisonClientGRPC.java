package ru.sinvic.client.comparison;

import ru.sinvic.client.grpc.GRPCClient;

import java.util.concurrent.CountDownLatch;

public class ComparisonClientGRPC {
    void sendRequests(int requestCount) {
        CountDownLatch latch = new CountDownLatch(requestCount);
        GRPCClient grpcClient = new GRPCClient(ComparisonConstants.HOST_COMMON, ComparisonConstants.PORT_GRPC);
        for (int i = 0; i < requestCount; i++) {
            grpcClient.request(latch);
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            grpcClient.shutDown();
        }
    }
}
