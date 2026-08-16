package com.github.rishikava;
import java.io.*;
import java.net.*;

public class Client {
    public static void main(String args[]) throws IOException {
        try (Socket socket = new Socket("localhost", 8080)) {
            // Send message
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
            );

            out.println("Hello world");

            // Read response
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }

            // Closes connection immediately
        }
    }
}
