package com.github.rishikava.http;

import java.util.Map;

public record HttpResponse (
    String version,
    int code,
    String status,
    Map<String, String> headers,
    byte[] body
) {
    public HttpResponse {
        if (!("HTTP/1.0".equals(version) || "HTTP/1.1".equals(version))) {
            throw new IllegalArgumentException("Only HTTP/1.0 and HTTP/1.1 supported");
        }

        if (code < 100 || code > 599) {
            throw new IllegalArgumentException("Invalid response code: " + code);
        }

        status =  status == null ? "Default status" : status;
        headers = headers == null ? Map.of() : Map.copyOf(headers);
        body = body == null ? new byte[0] : body.clone();
    }

    public static HttpResponse ok(String version, Map<String,String> headers, byte[] body) {
        return new HttpResponse(
            version,
            200,
            "OK",
            headers,
            body
        );
    }

    public static HttpResponse notFound(String version) {
        return new HttpResponse(
            version,
            404,
            "Not Found",
            Map.of("Content-Length", "0"),
            new byte[0]
        );
    }

    public static HttpResponse internalError(String version) {
        return new HttpResponse(
            version,
            500,
            "Internal Server Error",
            Map.of("Content-Length", "0"),
            new byte[0]
        );
    }

    public static HttpResponse requestTimeout(String version) {
        return new HttpResponse(
            version,
            408,
            "Request Timeout",
            Map.of("Content-Length", "0"),
            null
        );
    }
}
