package com.github.rishikava.server;
import java.io.*;
import java.net.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import com.github.rishikava.exceptions.*;
import com.github.rishikava.http.*;
import com.github.rishikava.util.HttpSerializer;

public class Server {
    private final Config config;
    private final ExecutorService executor;

    public Server(Config config) {
        this.config = config;
        this.executor = Executors.newFixedThreadPool(config.getMaxThreads());
    }

    public void run() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(config.getPort(), config.getBacklog(), InetAddress.getByName(config.getHost()))) {
            System.out.println("Server listening on port " + config.getPort());

            while (true) {
                Socket clientSocket = serverSocket.accept();
                clientSocket.setSoTimeout(config.getRequestTimeout());
                this.executor.submit(() -> handleClient(clientSocket));
            }
        }
    }

    private void handleClient(Socket clientSocket) {
        try (clientSocket) {
            System.out.println("Client connected: " + clientSocket.getInetAddress());

            OutputStream out = clientSocket.getOutputStream();

            try {
                HttpRequest request = config.getParser().parseRequest(clientSocket.getInputStream());
                HttpResponse response = config.getRouter().route(request);

                byte[] clientResponse = HttpSerializer.serialize(response);
                out.write(clientResponse);
            } catch (SocketTimeoutException e) {
                HttpResponse response = HttpResponse.requestTimeout("HTTP/1.X");
                out.write(HttpSerializer.serialize(response));
            } catch (BadRequestException e) {
                //TODO: server should not need to do this?
                HttpResponse response = new HttpResponse("HTTP/1.X", 400, "Bad Request", null, null);

                byte[] clientResponse = HttpSerializer.serialize(response);
                out.write(clientResponse);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
