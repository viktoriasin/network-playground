package ru.sinvic.server.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketServer {
    private static final Logger logger = LoggerFactory.getLogger(SocketServer.class);
    private static final int PORT = 8090;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);


    public static void main(String[] args) {
        new SocketServer().startSocket();
    }

    private void startSocket() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (!Thread.currentThread().isInterrupted()) {
                logger.info("waiting for client connection");
                Socket clientSocket = serverSocket.accept();
                executorService.submit(() -> handleClientConnection(clientSocket));
                }
        } catch (Exception ex) {
            logger.error("error", ex);
        }
        executorService.shutdown();
    }

    private void handleClientConnection(Socket clientSocket) {
        try (PrintWriter outputStream = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedReader inputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            int clientNumber = -1;
            while (!Thread.currentThread().isInterrupted()) {
                String stringFromClient = inputStream.readLine();
                if (stringFromClient == null || "stop".equals(stringFromClient)) {
                    logger.info("client {} finished", clientNumber);
                    return;
                }
                clientNumber = extractClientNumber(stringFromClient);
                logger.info("from client {}: {}", clientNumber, stringFromClient);
                outputStream.println(String.format("Response from server to client %d ! I got your message.", clientNumber));
            }
        } catch (Exception ex) {
            logger.error("error", ex);
        }
        logger.info("");
    }

    private int extractClientNumber(String stringFromClient) {
        return Integer.parseInt(String.valueOf(stringFromClient.charAt(stringFromClient.length() - 1)));
    }
}
