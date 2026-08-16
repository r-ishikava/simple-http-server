package com.github.rishikava;
import java.io.*;
import java.net.*;

import com.github.rishikava.http.HttpRequest;
import com.github.rishikava.http.HttpParser;

public class Server {
    public static void main(String[] args) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            System.out.println("Server listening on port 8080");

            HttpParser parser = new HttpParser();
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                HttpRequest request = parser.parseRequest(clientSocket.getInputStream());

                System.out.println("Method: " + request.method());
                System.out.println("Path: " + request.path());
                System.out.println("Version: " + request.version());
                System.out.println("Headers: " + request.headers());
                System.out.println("Body: " + request.body());
                
                // Handle client...
                clientSocket.close();
            }
        }
    }
}
