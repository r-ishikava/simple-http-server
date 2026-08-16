package com.github.rishikava.http;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class HttpParser {
    public HttpRequest parseRequest(InputStream inputStream) throws IOException {
        BufferedReader in = new BufferedReader(
            new InputStreamReader(inputStream)
        );

        // Parse request line
        String requestLine = in.readLine();
        String[] parts = requestLine.split(" ");
        String method = parts[0];
        String path = parts [1];
        String version = parts[2];

        // Parse headers
        Map<String, String> headers = new HashMap<>();
        String line;
        while (!(line = in.readLine()).isEmpty()) {
            String[] headerParts = line.split(": ", 2);
            headers.put(headerParts[0], headerParts[1]);
        }

        // Parse body
        String body = null;
        if (headers.containsKey("Content-Length")) {
            int length = Integer.parseInt(headers.get("Content-Length"));
            char[] buffer = new char[length];
            in.read(buffer, 0, length);
            body = new String(buffer);
        }

        return new HttpRequest(method, path, version, headers, body);
    }
}
