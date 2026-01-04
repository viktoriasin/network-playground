package ru.sinvic.client.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientSocket {
    private static final Logger logger = LoggerFactory.getLogger(ClientSocket.class);
    private static final int PORT = 8090;
    private static final String HOST = "localhost";

    public static void main(String[] args) {
        AtomicInteger counter = new AtomicInteger(0);
        for (int i = 0; i < 5; i++) {
            new Thread(() -> new ClientSocket().startWorking(counter.incrementAndGet())).start();
        }
    }

    private void startWorking(int clientNumber) {
        try {
            try (Socket clientSocket = new Socket(HOST, PORT)) {
                PrintWriter outputStream = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader inputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String msg = String.format("Hello from client %d", clientNumber);
                logger.info("sending to server: {}", msg);
                outputStream.println(msg);

                String responseMsg = inputStream.readLine();
                logger.info("server response: {}", responseMsg);
                Thread.sleep(TimeUnit.SECONDS.toMillis(3));
                logger.info("client {} stop communication with server", clientNumber);
                outputStream.println("stop");

                logger.info("");
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (Exception ex) {
            logger.error("error", ex);
        }
    }
}
