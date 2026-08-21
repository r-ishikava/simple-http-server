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

        // TODO code handling
        if (code < 100 || code > 599) {
            throw new IllegalArgumentException("Invalid response code: " + code);
        }

        status =  status == null ? "Default status" : status;
        headers = headers == null ? Map.of() : headers;
        body = body == null ? new byte[0] : body;
    }
}
