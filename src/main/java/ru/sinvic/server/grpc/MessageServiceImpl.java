package ru.sinvic.server.grpc;

import io.grpc.stub.StreamObserver;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sinvic.model.DataInnerObject;
import ru.sinvic.model.DataObject;
import ru.sinvic.server.InnerObject;
import ru.sinvic.server.MessageObject;
import ru.sinvic.server.MessageServiceGrpc;
import ru.sinvic.server.RequestMessage;
import ru.sinvic.server.util.DataObjectGeneratorService;

public class MessageServiceImpl extends MessageServiceGrpc.MessageServiceImplBase {
    private static final Logger logger = LoggerFactory.getLogger(MessageServiceImpl.class.getSimpleName());

    @Override
    public void getMessage(RequestMessage request, StreamObserver<MessageObject> responseObserver) {
        int requestId = request.getRequestId();
        logger.debug("request for new message accepted for request id: {}", requestId);
        MessageObject messageObject = generateMessageObject(requestId);
        responseObserver.onNext(messageObject);
        responseObserver.onCompleted();
        logger.debug("request for new message finished for request id: {}", requestId);
    }

    private MessageObject generateMessageObject(int requestId) {
        DataObject dataObject = DataObjectGeneratorService.generateDataObject(requestId);
        InnerObject dataInnerObject = convertInnerObject(dataObject.dataInnerObject());
        return MessageObject.newBuilder()
            .setRequestId(dataObject.requestId())
            .setUuid(String.valueOf(dataObject.uuid()))
            .setRandomBoolean(dataObject.randomBoolean())
            .setText(dataObject.text())
            .setInnerObject(dataInnerObject)
            .build();
    }

    private InnerObject convertInnerObject(@NonNull DataInnerObject dataInnerObject) {
        return InnerObject.newBuilder()
            .setRequestId(dataInnerObject.requestId())
            .setText(dataInnerObject.text())
            .build();
    }
}
