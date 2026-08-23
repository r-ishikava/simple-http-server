package com.github.rishikava.server;
import java.io.*;
import java.net.*;

import com.github.rishikava.exceptions.*;
import com.github.rishikava.http.*;
import com.github.rishikava.util.HttpSerializer;

public class Server {
    private final Config config;

    public Server(Config config) {
        this.config = config;
    }

    public void run() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(config.getPort(), config.getBacklog(), InetAddress.getByName(config.getHost()))) {
            System.out.println("Server listening on port " + config.getPort());

            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    System.out.println("Client connected: " + clientSocket.getInetAddress());

                    OutputStream out = clientSocket.getOutputStream();

                    try {
                        //TODO: non http requests hangs the server
                        HttpRequest request = config.getParser().parseRequest(clientSocket.getInputStream());
                        HttpResponse response = config.getRouter().route(request);

                        byte[] clientResponse = HttpSerializer.serialize(response);
                        out.write(clientResponse);
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
    }
}
