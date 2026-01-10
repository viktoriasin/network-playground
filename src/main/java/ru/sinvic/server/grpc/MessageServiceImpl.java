package ru.sinvic.server.grpc;

import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sinvic.server.Empty;
import ru.sinvic.server.MessageObject;
import ru.sinvic.server.MessageServiceGrpc;

public class MessageServiceImpl extends MessageServiceGrpc.MessageServiceImplBase {
    private static final Logger logger = LoggerFactory.getLogger("MessageServiceImpl");

    @Override
    public void getMessage(Empty request, StreamObserver<MessageObject> responseObserver) {
        logger.info("request for new message accepted..");
        MessageObject messageObject = dataObjectToMessageObject(new DataObject(1, 2L, false, "Hello world!"));
        responseObserver.onNext(messageObject);
        responseObserver.onCompleted();
        logger.info("request for new message finished..");
    }

    private MessageObject dataObjectToMessageObject(DataObject dataObject) {
        return MessageObject.newBuilder()
            .setNumber(dataObject.number())
            .setLongNumber(dataObject.longNumber())
            .setIsRandomNumber(dataObject.isRandomNumber())
            .setText(dataObject.text())
            .build();
    }
}
