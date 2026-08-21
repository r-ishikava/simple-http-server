package com.github.rishikava.util;

import java.util.Map;

import com.github.rishikava.http.HttpRequest;
import com.github.rishikava.http.HttpResponse;

public class HttpSerializer {
    public static byte[] serialize(HttpRequest request) {
        StringBuilder sb = new StringBuilder();

        sb.append(request.method()).append(' ')
          .append(request.path()).append(' ')
          .append(request.version()).append("\r\n");

        // TODO: Check Content-Length validation
        for (Map.Entry<String, String> entry : request.headers().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key).append(": ").append(value).append("\r\n");
        }

        sb.append("\r\n");
        sb.append(request.body());

        return sb.toString().getBytes();
    }

    public static byte[] serialize(HttpResponse response) {
        StringBuilder sb = new StringBuilder();

        sb.append(response.version()).append(' ')
          .append(response.code()).append(' ')
          .append(response.status()).append("\r\n");

        // TODO: Check Content-Length validation
        for (Map.Entry<String, String> entry : response.headers().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key).append(": ").append(value).append("\r\n");
        }

        sb.append("\r\n");

        byte[] bodylessResponse = sb.toString().getBytes();
        byte[] body = response.body();
        byte[] byteResponse = new byte[bodylessResponse.length + body.length];

        System.arraycopy(bodylessResponse, 0, byteResponse, 0, bodylessResponse.length);
        System.arraycopy(body, 0, byteResponse, bodylessResponse.length, body.length);

        return byteResponse;
    }
    
    public static HttpRequest deserializeRequest(String request) {
        throw new UnsupportedOperationException();
    }

    public static HttpResponse deserializeResponse(String response) {
        throw new UnsupportedOperationException();
    }
}
