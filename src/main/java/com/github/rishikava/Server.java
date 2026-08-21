package com.github.rishikava;
import java.io.*;
import java.net.*;

import com.github.rishikava.handler.EchoHandler;
import com.github.rishikava.http.*;
import com.github.rishikava.router.*;
import com.github.rishikava.util.*;

// TODO: Implement a separate entry point with the server config
public class Server {
    public static void main(String[] args) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            System.out.println("Server listening on port 8080");

            HttpParser parser = new HttpParser();
            Router router = new Router();
            EchoHandler handler = new EchoHandler();
            router.register(new Route(HttpMethod.POST, "/echo"), handler);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                //TODO: non http requests hangs the server
                HttpRequest request = parser.parseRequest(clientSocket.getInputStream());
                HttpResponse response = router.route(request);

                OutputStream out = clientSocket.getOutputStream();

                byte[] client_response = HttpSerializer.serialize(response);
                out.write(client_response);
                
                // Handle client...
                clientSocket.close();
            }
        }
    }
}
