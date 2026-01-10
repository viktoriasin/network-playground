package ru.sinvic.server.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.Executors;

public class GRPCServer {
    private static final Logger logger = LoggerFactory.getLogger("GRPCServer");

    public static final int SERVER_PORT = 8190;

    public static void main(String[] args) throws IOException, InterruptedException {
        MessageServiceImpl messageService = new MessageServiceImpl();
        Server server = ServerBuilder
            .forPort(SERVER_PORT)
            .addService(messageService)
            .executor(Executors.newFixedThreadPool(10))
            .build();

        server.start();
        logger.info("grpc server waiting for client connections...");

        server.awaitTermination();
    }
}
