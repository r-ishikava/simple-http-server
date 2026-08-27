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
    private final HttpParser parser;

    public Server(Config config) {
        this.config = config;
        this.executor = Executors.newFixedThreadPool(config.getMaxThreads());
        this.parser = new HttpParser();
        this.parser.setMaxLineSize(this.config.getMaxLineSize());
        this.parser.setMaxHeaderCount(this.config.getMaxHeaderCount());
        this.parser.setMaxBodySize(this.config.getMaxBodySize());
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

    void handleClient(Socket clientSocket) {
        try (clientSocket;
             BufferedInputStream in = new BufferedInputStream(clientSocket.getInputStream());
             OutputStream out = clientSocket.getOutputStream();
        ) {
            while (true) {
                HttpRequest request;
                HttpResponse response;
                try {
                    request = this.parser.parseRequest(in);
                } catch (SocketTimeoutException e) {
                    break;
                } catch (BadRequestException e) {
                    response = new HttpResponse("HTTP/1.1", 400, "Bad Request", null, null);
                    out.write(HttpSerializer.serialize(response));
                    break;
                } catch (RequestTooLargeException e) {
                    response = new HttpResponse("HTTP/1.1", 413, "Request Too Large", null, null);
                    out.write(HttpSerializer.serialize(response));
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
                boolean keepAlive = shouldKeepAlive(request);
                response = config.getRouter().route(request);
                out.write(HttpSerializer.serialize(response));
                if (!keepAlive) break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    boolean shouldKeepAlive(HttpRequest request) {
        boolean hasConnectionHeader = request.headers().containsKey("Connection");
        if (hasConnectionHeader) {
            String connectionTokens = request.headers().get("Connection");
            if (connectionTokens.toLowerCase().contains("close")) return false;
            else if (connectionTokens.toLowerCase().contains("keep-alive")) return true;
        }
        if ("HTTP/1.1".equals(request.version())) return true;
        if ("HTTP/1.0".equals(request.version())) return false;
        return false;
    }
}
