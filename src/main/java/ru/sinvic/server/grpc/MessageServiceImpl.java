package ru.sinvic.server.grpc;

import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sinvic.model.DataObject;
import ru.sinvic.server.Empty;
import ru.sinvic.server.InnerObject;
import ru.sinvic.server.MessageObject;
import ru.sinvic.server.MessageServiceGrpc;

import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class MessageServiceImpl extends MessageServiceGrpc.MessageServiceImplBase {
    private static final Logger logger = LoggerFactory.getLogger("MessageServiceImpl");

    private static final String INNER_OBJECT_STRING_FIELD = "Inner object";
    private static final int DATA_OBJECT_STRING_FIELD_LENGTH = 1000;

    private final AtomicInteger requestNumber = new AtomicInteger(0);
    private final Random random = new Random();

    @Override
    public void getMessage(Empty request, StreamObserver<MessageObject> responseObserver) {
        logger.info("request for new message accepted..");
        MessageObject messageObject = generateMessageObject();
        responseObserver.onNext(messageObject);
        responseObserver.onCompleted();
        logger.info("request for new message finished..");
    }

    private MessageObject generateMessageObject() {
        int requestId = requestNumber.incrementAndGet();
        UUID uuid = UUID.randomUUID();
        boolean randomBoolean = random.nextBoolean();
        String randomString = generateRandomStringFromBytesArray();
        InnerObject dataInnerObject = generateInnerObject(requestId);
        return MessageObject.newBuilder()
            .setRequestId(requestId)
            .setUuid(String.valueOf(uuid))
            .setRandomBoolean(randomBoolean)
            .setText(randomString)
            .setInnerObject(dataInnerObject)
            .build();
    }

    private InnerObject generateInnerObject(int requestId) {
        return InnerObject.newBuilder()
            .setRequestId(requestId)
            .setText(INNER_OBJECT_STRING_FIELD)
            .build();
    }

    private String generateRandomStringFromBytesArray() {
        byte[] arrayBytes = new byte[DATA_OBJECT_STRING_FIELD_LENGTH];

        for (int i = 0; i < DATA_OBJECT_STRING_FIELD_LENGTH; i++) {
            arrayBytes[i] = (byte) (32 + random.nextInt(95));
        }

        return new String(arrayBytes, StandardCharsets.UTF_8);
    }
}
