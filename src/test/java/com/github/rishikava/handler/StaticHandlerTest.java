package com.github.rishikava.handler;

import com.github.rishikava.http.HttpMethod;
import com.github.rishikava.http.HttpRequest;
import com.github.rishikava.http.HttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StaticHandlerTest {

    @TempDir
    Path tempDir;

    private StaticHandler staticHandler;
    private Path testFile;

    @BeforeEach
    void setUp() throws IOException {
        testFile = tempDir.resolve("index.html");
        Files.writeString(testFile, "<html>Hello Static</html>");
        staticHandler = new StaticHandler(tempDir);
    }

    @Test
    @DisplayName("Should serve static file with 200 OK and correct headers")
    void shouldServeStaticFile() {
        HttpRequest request = new HttpRequest(
            HttpMethod.GET,
            "/static/index.html",
            "HTTP/1.1",
            Map.of("Host", "localhost"),
            null
        );

        HttpResponse response = staticHandler.handle(request);

        assertEquals(200, response.code());
        assertEquals("OK", response.status());
        assertEquals("text/html", response.headers().get("Content-Type"));
        assertEquals("25", response.headers().get("Content-Length"));
        assertEquals("<html>Hello Static</html>", new String(response.body()));
    }

    @Test
    @DisplayName("Should return 404 for non-existent file")
    void shouldHandleFileNotFound() {
        HttpRequest request = new HttpRequest(
            HttpMethod.GET,
            "/static/nonexistent.html",
            "HTTP/1.1",
            Map.of("Host", "localhost"),
            null
        );

        HttpResponse response = staticHandler.handle(request);
        assertEquals(404, response.code());
    }

    @Test
    @DisplayName("Should prevent path traversal attacks")
    void shouldPreventPathTraversal() {
        HttpRequest request = new HttpRequest(
            HttpMethod.GET,
            "/static/../../secret.txt",
            "HTTP/1.1",
            Map.of("Host", "localhost"),
            null
        );

        HttpResponse response = staticHandler.handle(request);
        assertTrue(response.code() == 400 || response.code() == 404 || response.code() == 403);
    }

    @Test
    @DisplayName("Should use octet-stream mime type for unknown extension")
    void shouldFallbackMimeType() throws IOException {
        Path unknownFile = tempDir.resolve("data.unknownext");
        Files.writeString(unknownFile, "some data");

        HttpRequest request = new HttpRequest(
            HttpMethod.GET,
            "/static/data.unknownext",
            "HTTP/1.1",
            Map.of("Host", "localhost"),
            null
        );

        HttpResponse response = staticHandler.handle(request);
        assertEquals(200, response.code());
        assertEquals("application/octet-stream", response.headers().get("Content-Type"));
    }

    @Test
    @DisplayName("Should serve zero-byte file")
    void shouldServeZeroByteFile() throws IOException {
        Path emptyFile = tempDir.resolve("empty.txt");
        Files.writeString(emptyFile, "");

        HttpRequest request = new HttpRequest(
            HttpMethod.GET,
            "/static/empty.txt",
            "HTTP/1.1",
            Map.of("Host", "localhost"),
            null
        );

        HttpResponse response = staticHandler.handle(request);
        assertEquals(200, response.code());
        assertEquals("0", response.headers().get("Content-Length"));
        assertEquals(0, response.body().length);
    }

    @Test
    @DisplayName("Should handle hidden file request")
    void shouldHandleHiddenFile() throws IOException {
        Path hiddenFile = tempDir.resolve(".env");
        Files.writeString(hiddenFile, "SECRET=123");

        HttpRequest request = new HttpRequest(
            HttpMethod.GET,
            "/static/.env",
            "HTTP/1.1",
            Map.of("Host", "localhost"),
            null
        );

        HttpResponse response = staticHandler.handle(request);
        // Serving hidden files should not be allowed
        assertEquals(404, response.code());
    }

    @Test
    @DisplayName("Should handle directory request")
    void shouldHandleDirectoryRequest() {
        HttpRequest request = new HttpRequest(
            HttpMethod.GET,
            "/static/",
            "HTTP/1.1",
            Map.of("Host", "localhost"),
            null
        );

        HttpResponse response = staticHandler.handle(request);
        assertTrue(response.code() >= 400);
    }
}
