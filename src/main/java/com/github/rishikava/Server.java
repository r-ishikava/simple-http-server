package com.github.rishikava;
import java.io.*;
import java.net.*;
import java.nio.file.Path;

import com.github.rishikava.handler.*;
import com.github.rishikava.http.*;
import com.github.rishikava.router.*;
import com.github.rishikava.util.*;
import com.github.rishikava.exceptions.*;

// TODO: Implement a separate entry point with the server config
public class Server {
    public static void main(String[] args) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            System.out.println("Server listening on port 8080");

            HttpParser parser = new HttpParser();
            Router router = new Router();
            EchoHandler handler = new EchoHandler();
            StaticHandler handler2 = new StaticHandler(Path.of("/home/rafael/projects/http-server/static"));
            router.register(new Route(HttpMethod.POST, "/echo"), handler);
            router.register(new Route(HttpMethod.GET, "/static/image.png"), handler2);
            router.register(new Route(HttpMethod.GET, "/static/index.html"), handler2);

            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    System.out.println("Client connected: " + clientSocket.getInetAddress());

                    OutputStream out = clientSocket.getOutputStream();

                    try {
                        //TODO: non http requests hangs the server
                        HttpRequest request = parser.parseRequest(clientSocket.getInputStream());
                        HttpResponse response = router.route(request);

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
