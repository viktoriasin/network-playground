package ru.sinvic.server.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketServer {
    private static final Logger logger = LoggerFactory.getLogger(SocketServer.class);
    private static final int PORT = 8090;


    public static void main(String[] args) {
        new SocketServer().startSocket();
    }

    private void startSocket() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (!Thread.currentThread().isInterrupted()) {
                logger.info("waiting for client connection");
                try (Socket clientSocket = serverSocket.accept()) {
                    handleClientConnection(clientSocket);
                }
            }
        } catch (Exception ex) {
            logger.error("error", ex);
        }
    }

    private static void handleClientConnection(Socket clientSocket) {
        try (PrintWriter outputStream = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedReader inputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            String stringFromClient = null;
            while (!"stop".equals(stringFromClient)) {
                stringFromClient = inputStream.readLine();
                if (stringFromClient != null) {
                    logger.info("from client: {}", stringFromClient);
                    outputStream.println("Response from server! I got your message.");
                }
            }

        } catch (Exception ex) {
            logger.error("error", ex);
        }
        logger.info("");
    }
}
