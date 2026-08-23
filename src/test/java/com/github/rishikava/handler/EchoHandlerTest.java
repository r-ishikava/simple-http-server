package com.github.rishikava.handler;

import com.github.rishikava.http.HttpMethod;
import com.github.rishikava.http.HttpRequest;
import com.github.rishikava.http.HttpResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EchoHandlerTest {

    @Test
    @DisplayName("Should echo request body and content length")
    void shouldEchoRequest() {
        EchoHandler handler = new EchoHandler();
        String bodyContent = "Hello, Echo!";
        HttpRequest request = new HttpRequest(
            HttpMethod.POST,
            "/echo",
            "HTTP/1.1",
            Map.of("Host", "localhost", "Content-Length", String.valueOf(bodyContent.length())),
            bodyContent
        );

        HttpResponse response = handler.handle(request);

        assertEquals(200, response.code());
        assertEquals("OK", response.status());
        assertEquals("text/plain", response.headers().get("Content-Type"));
        assertEquals(String.valueOf(bodyContent.length()), response.headers().get("Content-Length"));
        assertEquals(bodyContent, new String(response.body()));
    }

    @Test
    @DisplayName("Should handle echo with empty body")
    void shouldHandleEchoWithEmptyBody() {
        EchoHandler handler = new EchoHandler();
        HttpRequest request = new HttpRequest(
            HttpMethod.POST,
            "/echo",
            "HTTP/1.1",
            Map.of("Host", "localhost", "Content-Length", "0"),
            ""
        );

        HttpResponse response = handler.handle(request);
        assertEquals(200, response.code());
        assertEquals("", new String(response.body()));
        assertEquals("0", response.headers().get("Content-Length"));
    }
}
