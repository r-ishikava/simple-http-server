package com.github.rishikava.http;

import java.util.Map;

public record HttpRequest (
    HttpMethod method,
    String path,
    String version,
    Map<String, String> headers,
    String body
) {
    public HttpRequest {
        if (path == null || !path.startsWith("/")) {
            throw new IllegalArgumentException("Path must start with '/'");
        }

        if (!version.equals("HTTP/1.1") && !version.equals("HTTP/1.0")) {
            throw new IllegalArgumentException("Only HTTP/1.0 and HTTP/1.1 supported");
        }

        if (!headers.containsKey("Host") && !version.equals("HTTP/1.0")) {
            throw new IllegalArgumentException("Missing Host header");
        }

        if (headers.containsKey("Content-Length")) {
            try {
                Integer.parseInt(headers.get("Content-Length"));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid Content-Length");
            }
        }
    }
}
