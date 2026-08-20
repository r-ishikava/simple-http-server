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

            out.print(
                "GET / HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "\r\n"
            );
            out.flush();

            // Read response
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }

            // Closes connection immediately
        }
    }
}
