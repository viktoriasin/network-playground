package ru.sinvic.client.socket.nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class NIOClientSocket {
    private static final Logger logger = LoggerFactory.getLogger(NIOClientSocket.class);

    private static final int PORT = 8080;
    private static final String HOST = "localhost";

    public static void main(String[] args) {
        new Thread(() -> new NIOClientSocket().startWorking("client-1")).start();
        new Thread(() -> new NIOClientSocket().startWorking("client-2")).start();
    }

    private void startWorking(String clientName) {
        try {
            try (SocketChannel socketChannel = SocketChannel.open()) {
                socketChannel.configureBlocking(false);

                socketChannel.connect(new InetSocketAddress(HOST, PORT));
                logger.info("{} trying to connect to server", clientName);
                if (socketChannel.finishConnect()) {
                    logger.info("{} connected to server", clientName);
                }
                sendData(socketChannel, clientName);
                handleServerResponse(socketChannel, clientName);
            }
        } catch (IOException ex) {
            logger.error("exception", ex);
        }
    }

    private void sendData(SocketChannel socketChannel, String clientName) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1000);
        buffer.put(("hello from " + clientName).getBytes());
        buffer.flip();
        logger.info("{} sending data to server", clientName);
        socketChannel.write(buffer);
    }

    private void handleServerResponse(SocketChannel socketChannel, String clientName) throws IOException {
        try (Selector selector = Selector.open()) {
            socketChannel.register(selector, SelectionKey.OP_READ);
            logger.info("{} is waiting for server response", clientName);
            selector.select(selectionKey -> {
                if (selectionKey.isReadable()) {
                    processServerResponse((SocketChannel) selectionKey.channel(), clientName);
                }
            });
        }
    }

    private void processServerResponse(SocketChannel socketChannel, String clientName) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(200);

            StringBuilder response = new StringBuilder();
            logger.info("{} is processing server response", clientName);
            while (socketChannel.read(buffer) > 0) {
                buffer.flip();

                String responsePart = StandardCharsets.UTF_8.decode(buffer).toString();
                logger.info("{} responsePart: {}", clientName, responsePart);

                response.append(responsePart);
                buffer.flip();
            }

            logger.info("server response for client {}: {}", clientName, response);
        } catch (Exception ex) {
            logger.error("exception", ex);
        }
    }
}
