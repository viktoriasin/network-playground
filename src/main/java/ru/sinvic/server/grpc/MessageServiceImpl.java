package ru.sinvic.server.grpc;

import io.grpc.stub.StreamObserver;
import ru.sinvic.server.Empty;
import ru.sinvic.server.MessageObject;
import ru.sinvic.server.MessageServiceGrpc;

public class MessageServiceImpl extends MessageServiceGrpc.MessageServiceImplBase {
    @Override
    public void getMessage(Empty request, StreamObserver<MessageObject> responseObserver) {
        MessageObject messageObject = dataObjectToMessageObject(new DataObject(1, 2L, false, "Hello world!"));
        responseObserver.onNext(messageObject);
        responseObserver.onCompleted();
    }

    private MessageObject dataObjectToMessageObject(DataObject dataObject) {
        return MessageObject.newBuilder()
            .setNumber(dataObject.getNumber())
            .setLongNumber(dataObject.getLongNumber())
            .setIsRandomNumber(dataObject.isRandomNumber())
            .setText(dataObject.getText())
            .build();
    }
}
