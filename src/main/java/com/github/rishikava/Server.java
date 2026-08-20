package com.github.rishikava;
import java.io.*;
import java.net.*;

import com.github.rishikava.http.*;
import com.github.rishikava.router.*;
import com.github.rishikava.util.*;

public class Server {
    public static void main(String[] args) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            System.out.println("Server listening on port 8080");

            HttpParser parser = new HttpParser();
            Router router = new Router();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                //TODO: non http requests hangs the server
                HttpRequest request = parser.parseRequest(clientSocket.getInputStream());
                HttpResponse response = router.route(request);

                PrintWriter out = new PrintWriter(
                    clientSocket.getOutputStream(), true
                );

                String client_response = HttpSerializer.serialize(response);
                out.println(client_response);
                
                // Handle client...
                clientSocket.close();
            }
        }
    }
}
