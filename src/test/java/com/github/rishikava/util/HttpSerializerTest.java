package com.github.rishikava.util;

import com.github.rishikava.http.HttpMethod;
import com.github.rishikava.http.HttpRequest;
import com.github.rishikava.http.HttpResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HttpSerializerTest {

    @Test
    @DisplayName("Should correctly serialize HTTP request")
    void shouldSerializeRequest() {
        HttpRequest request = new HttpRequest(
            HttpMethod.POST,
            "/submit",
            "HTTP/1.1",
            Map.of("Host", "example.com", "Content-Length", "5"),
            "hello"
        );

        byte[] serialized = HttpSerializer.serialize(request);
        String result = new String(serialized, StandardCharsets.US_ASCII);

        assertTrue(result.startsWith("POST /submit HTTP/1.1\r\n"));
        assertTrue(result.contains("Host: example.com\r\n"));
        assertTrue(result.contains("Content-Length: 5\r\n"));
        assertTrue(result.endsWith("\r\n\r\nhello"));
    }

    @Test
    @DisplayName("Should correctly serialize HTTP response with body")
    void shouldSerializeResponse() {
        byte[] bodyBytes = "Response body data".getBytes(StandardCharsets.UTF_8);
        HttpResponse response = new HttpResponse(
            "HTTP/1.1",
            200,
            "OK",
            Map.of("Content-Type", "text/plain"),
            bodyBytes
        );

        byte[] serialized = HttpSerializer.serialize(response);
        String serializedStr = new String(serialized, StandardCharsets.UTF_8);

        assertTrue(serializedStr.startsWith("HTTP/1.1 200 OK\r\n"));
        assertTrue(serializedStr.contains("Content-Type: text/plain\r\n"));
        assertTrue(serializedStr.endsWith("Response body data"));
    }

    @Test
    @DisplayName("Should serialize request with null or empty body")
    void shouldSerializeRequestWithEmptyBody() {
        HttpRequest request = new HttpRequest(
            HttpMethod.GET,
            "/",
            "HTTP/1.1",
            Map.of("Host", "localhost"),
            null
        );

        byte[] serialized = HttpSerializer.serialize(request);
        assertNotNull(serialized);
    }

    @Test
    @DisplayName("Should serialize response with null body")
    void shouldSerializeResponseWithNullBody() {
        HttpResponse response = new HttpResponse(
            "HTTP/1.1",
            204,
            "No Content",
            null,
            null
        );

        byte[] serialized = HttpSerializer.serialize(response);
        assertNotNull(serialized);
        assertTrue(new String(serialized, StandardCharsets.UTF_8).contains("HTTP/1.1 204 No Content"));
    }
}
