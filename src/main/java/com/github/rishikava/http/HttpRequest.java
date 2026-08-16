package com.github.rishikava.http;

import java.util.Map;

public record HttpRequest (
    String method,
    String path,
    String version,
    Map<String, String> headers,
    String body
) {
    public HttpRequest {
        if (!method.matches("GET|POST|PUT|DELETE|HEAD|OPTIONS")) {
            throw new IllegalArgumentException("Invalid meethod: " + method);
        }

        if (path == null || !path.startsWith("/")) {
            throw new IllegalArgumentException("Path must start with '/'");
        }

        if (!version.equals("HTTP/1.1")) {
            throw new IllegalArgumentException("Only HTTP/1.1 supported");
        }

        if (!headers.containsKey("Host")) {
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
