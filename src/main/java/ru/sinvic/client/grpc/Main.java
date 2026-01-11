package ru.sinvic.client.grpc;

import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sinvic.model.DataInnerObject;
import ru.sinvic.model.DataObject;
import ru.sinvic.server.Empty;
import ru.sinvic.server.InnerObject;
import ru.sinvic.server.MessageObject;
import ru.sinvic.server.MessageServiceGrpc;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8190;
    private static final int REQUEST_COUNT = 5;

    public static void main(String[] args) throws InterruptedException {
        var channel = ManagedChannelBuilder.forAddress(SERVER_HOST, SERVER_PORT)
            .usePlaintext()
            .build();

        CountDownLatch latch = new CountDownLatch(REQUEST_COUNT);
        var stub = MessageServiceGrpc.newStub(channel);
        logger.info("using server stub to get message...");
        for (int i = 0; i < REQUEST_COUNT; i++) {
            stub.getMessage(Empty.getDefaultInstance(), new StreamObserver<MessageObject>() {
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
                    logger.info("finished server response");
                    latch.countDown();
                }
            });
        }


        latch.await();
        channel.shutdown();
    }

    public static DataObject messageObjectToDataObject(MessageObject messageObject) {
        DataInnerObject dataInnerObject = getDataInnerObject(messageObject.getInnerObject());
        return new DataObject(messageObject.getRequestId(), UUID.fromString(messageObject.getUuid()), messageObject.getRandomBoolean(), messageObject.getText(), dataInnerObject);
    }

    private static DataInnerObject getDataInnerObject(InnerObject innerObject) {
        return new DataInnerObject(innerObject.getRequestId(), innerObject.getText());
    }
}