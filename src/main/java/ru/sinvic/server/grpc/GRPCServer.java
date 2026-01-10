package ru.sinvic.server.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class GRPCServer {
    private static final Logger logger = LoggerFactory.getLogger("NIOSocketServer");

    public static final int SERVER_PORT = 8190;

    public static void main(String[] args) throws IOException, InterruptedException {
        MessageServiceImpl messageService = new MessageServiceImpl();
        Server server = ServerBuilder.forPort(SERVER_PORT).addService(messageService).build();

        server.start();
        logger.info("grpc server waiting for client connections...");

        server.awaitTermination();
    }
}
