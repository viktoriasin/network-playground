package ru.sinvic.server.socket.nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

// TODO: Merge with other server stuff
public class NIOSocketServer {
    private static final Logger logger = LoggerFactory.getLogger(NIOSocketServer.class);
    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        new NIOSocketServer().doWork();
    }

    private void doWork() throws IOException {
        try (ServerSocketChannel socketChannel = ServerSocketChannel.open()) {
            socketChannel.configureBlocking(false);
            socketChannel.bind(new InetSocketAddress(PORT));

            try (Selector selector = Selector.open()) {
                socketChannel.register(selector, SelectionKey.OP_ACCEPT);

                while (!Thread.currentThread().isInterrupted()) {
                    logger.info("waiting for client");
                    selector.select(this::processIO);
                }
            }
        }
    }

    private void processIO(SelectionKey selectionKey) {
        try {
            logger.info("something happened, key:{}", selectionKey);
            if (selectionKey.isAcceptable()) {
                acceptClient(selectionKey);
            } else if (selectionKey.isReadable()) {
                handleClientRequest(selectionKey);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void acceptClient(SelectionKey selectionKey) throws IOException {
        Selector selector = selectionKey.selector();
        logger.info("accept client connection, key:{}, selector:{}", selectionKey, selector);

        SocketChannel clientSocketChannel;
        ServerSocketChannel channel = (ServerSocketChannel) selectionKey.channel();
        clientSocketChannel = channel.accept();

        clientSocketChannel.configureBlocking(false);
        clientSocketChannel.register(selector, SelectionKey.OP_READ);
        logger.info("clientSocketChannel:{}", clientSocketChannel);
    }

    private void handleClientRequest(SelectionKey selectionKey) throws IOException {
        SocketChannel clientSocketChannel = (SocketChannel) selectionKey.channel();
        logger.info("reading from clientSocketChannel:{}", clientSocketChannel);

        String clientRequest = parseClientRequest(clientSocketChannel);

        if ("stop".equals(clientRequest)) {
            logger.info("close clientSocketChannel:{}", clientSocketChannel);
            clientSocketChannel.close();
        } else {
            logger.info("send response to clientSocketChannel:{}", clientSocketChannel);
            sendResponseToClient(clientSocketChannel, clientRequest);
        }

    }

    private String parseClientRequest(SocketChannel clientSocketChannel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(5);
        StringBuilder clientRequestRaw = new StringBuilder(100);

        while (clientSocketChannel.read(buffer) > 0) {
            buffer.flip();
            String input = StandardCharsets.UTF_8.decode(buffer).toString();
            logger.info("read part of client request: {}", input);
            buffer.flip();
            clientRequestRaw.append(input);
        }

        String clientRequest = clientRequestRaw.toString().replace("\n", "").replace("\r", "");
        logger.info("client request: {} ", clientRequest);
        return clientRequest;
    }

    private void sendResponseToClient(SocketChannel clientSocketChannel, String clientRequest) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(5);
        byte[] bytes = ("server accept " + clientRequest).getBytes();

        for (byte responseByte : bytes) {
            buffer.put(responseByte);

            if (buffer.position() == buffer.limit()) {
                buffer.flip();
                clientSocketChannel.write(buffer);
                buffer.flip();
            }
        }

        if (buffer.hasRemaining()) {
            buffer.flip();
            clientSocketChannel.write(buffer);
        }
    }
}
