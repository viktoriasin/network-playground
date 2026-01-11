package ru.sinvic.client.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sinvic.model.DataInnerObject;
import ru.sinvic.model.DataObject;
import ru.sinvic.server.InnerObject;
import ru.sinvic.server.MessageObject;
import ru.sinvic.server.MessageServiceGrpc;
import ru.sinvic.server.RequestMessage;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class GRPCClient {
    private static final Logger logger = LoggerFactory.getLogger(GRPCClient.class);

    private static final String DEFAULT_SERVER_HOST = "localhost";
    private static final int DEFAULT_SERVER_PORT = 8190;
    private static final int DEFAULT_REQUEST_COUNT = 5;

    private final String host;
    private final int port;
    private final int requestCount;

    private ManagedChannel channel;
    MessageServiceGrpc.MessageServiceStub stub;

    private final AtomicInteger requestId = new AtomicInteger(0);

    public GRPCClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.requestCount = DEFAULT_REQUEST_COUNT;
        setChannelAndStub();
    }

    public GRPCClient() {
        this(DEFAULT_SERVER_HOST, DEFAULT_SERVER_PORT);
    }

    public static void main(String[] args) throws InterruptedException {
        new GRPCClient().run();
    }

    public void request(CountDownLatch latch) throws InterruptedException {
        RequestMessage requestMessage = generateRequestMessage();
        this.stub.getMessage(requestMessage, getResponseObserver(latch));
    }

    public void shutDown() {
        this.channel.shutdown();
    }

    public void run() throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(requestCount);

        for (int i = 0; i < requestCount; i++) {
            RequestMessage requestMessage = generateRequestMessage();
            logger.info("send request to server with request id {}", requestMessage.getRequestId());

            this.stub.getMessage(requestMessage, getResponseObserver(latch));
        }


        latch.await();
        this.channel.shutdown();
    }

    private void setChannelAndStub() {
        this.channel = createChannel();
        this.stub = MessageServiceGrpc.newStub(channel);
    }

    private ManagedChannel createChannel() {
        return ManagedChannelBuilder.forAddress(host, port)
            .usePlaintext()
            .build();
    }

    private RequestMessage generateRequestMessage() {
        return RequestMessage.newBuilder().setRequestId(requestId.incrementAndGet()).build();
    }

    private StreamObserver<MessageObject> getResponseObserver(CountDownLatch latch) {
        return new StreamObserver<MessageObject>() {
            @Override
            public void onNext(MessageObject messageObject) {
                logger.info("get response message {} from server", messageObjectToDataObject(messageObject));
            }

            @Override
            public void onError(Throwable throwable) {
                logger.error(throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                logger.info("finished server response..");
                latch.countDown();
            }
        };
    }

    public static DataObject messageObjectToDataObject(MessageObject messageObject) {
        DataInnerObject dataInnerObject = getDataInnerObject(messageObject.getInnerObject());
        return new DataObject(messageObject.getRequestId(), UUID.fromString(messageObject.getUuid()), messageObject.getRandomBoolean(), messageObject.getText(), dataInnerObject);
    }

    private static DataInnerObject getDataInnerObject(InnerObject innerObject) {
        return new DataInnerObject(innerObject.getRequestId(), innerObject.getText());
    }
}