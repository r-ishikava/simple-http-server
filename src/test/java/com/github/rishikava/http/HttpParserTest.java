package com.github.rishikava.http;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HttpParserTest {
    private HttpParser parser;

    @BeforeEach
    void setUp() {
        parser = new HttpParser();
    }

    private HttpRequest parse(String raw) throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(raw.getBytes());
        return parser.parseRequest(input);
    }

    // ==================== BASIC REQUESTS ====================
    
    @Test
    @DisplayName("Should parse simple GET request")
    void shouldParseGetRequest() throws IOException {
        String raw = "GET /index.html HTTP/1.1\r\n" +
                     "Host: localhost:8080\r\n" +
                     "User-Agent: curl/7.68.0\r\n" +
                     "Accept: */*\r\n" +
                     "\r\n";
        
        HttpRequest request = parse(raw);
        
        assertEquals(HttpMethod.GET, request.method());
        assertEquals("/index.html", request.path());
        assertEquals("HTTP/1.1", request.version());
        
        Map<String, String> headers = request.headers();
        assertEquals("localhost:8080", headers.get("Host"));
        assertEquals("curl/7.68.0", headers.get("User-Agent"));
        assertEquals("*/*", headers.get("Accept"));
        
        assertNull(request.body());
    }
    
    @Test
    @DisplayName("Should parse GET with query parameters")
    void shouldParseGetWithQueryParams() throws IOException {
        String raw = "GET /search?q=java&page=2 HTTP/1.1\r\n" +
                     "Host: google.com\r\n" +
                     "\r\n";
        
        HttpRequest request = parse(raw);
        
        assertEquals("/search?q=java&page=2", request.path());
    }
    
    // ==================== POST REQUESTS ====================
    
    @Test
    @DisplayName("Should parse POST with JSON body")
    void shouldParsePostWithJsonBody() throws IOException {
        String raw = "POST /api/users HTTP/1.1\r\n" +
                     "Host: api.example.com\r\n" +
                     "Content-Type: application/json\r\n" +
                     "Content-Length: 24\r\n" +
                     "\r\n" +
                     "{\"name\":\"John\",\"age\":30}";
        
        HttpRequest request = parse(raw);
        
        assertEquals(HttpMethod.POST, request.method());
        assertEquals("/api/users", request.path());
        assertEquals("application/json", request.headers().get("Content-Type"));
        assertEquals("24", request.headers().get("Content-Length"));
        assertEquals("{\"name\":\"John\",\"age\":30}", request.body());
    }
    
    @Test
    @DisplayName("Should parse POST with form data")
    void shouldParsePostWithFormData() throws IOException {
        String raw = "POST /login HTTP/1.1\r\n" +
                     "Host: example.com\r\n" +
                     "Content-Type: application/x-www-form-urlencoded\r\n" +
                     "Content-Length: 32\r\n" +
                     "\r\n" +
                     "username=johndoe&password=secret";
        
        HttpRequest request = parse(raw);
        
        assertEquals(HttpMethod.POST, request.method());
        assertEquals("username=johndoe&password=secret", request.body());
    }
    
    @Test
    @DisplayName("Should parse POST with empty body")
    void shouldParsePostWithEmptyBody() throws IOException {
        String raw = "POST /api/users HTTP/1.1\r\n" +
                     "Host: example.com\r\n" +
                     "Content-Length: 0\r\n" +
                     "\r\n" +
                     "";
        
        HttpRequest request = parse(raw);
        
        assertEquals(HttpMethod.POST, request.method());
        assertEquals("", request.body());
    }
    
    // ==================== OTHER METHODS ====================
    
    @Test
    @DisplayName("Should parse PUT request")
    void shouldParsePutRequest() throws IOException {
        String raw = "PUT /files/document.txt HTTP/1.1\r\n" +
                     "Host: storage.example.com\r\n" +
                     "Content-Length: 20\r\n" +
                     "\r\n" +
                     "This is file content";
        
        HttpRequest request = parse(raw);
        
        assertEquals(HttpMethod.PUT, request.method());
        assertEquals("/files/document.txt", request.path());
        assertEquals("This is file content", request.body());
    }
    
    @Test
    @DisplayName("Should parse DELETE request")
    void shouldParseDeleteRequest() throws IOException {
        String raw = "DELETE /users/42 HTTP/1.1\r\n" +
                     "Host: api.example.com\r\n" +
                     "\r\n";
        
        HttpRequest request = parse(raw);
        
        assertEquals(HttpMethod.DELETE, request.method());
        assertEquals("/users/42", request.path());
        assertNull(request.body());
    }
    
    @Test
    @DisplayName("Should parse HEAD request")
    void shouldParseHeadRequest() throws IOException {
        String raw = "HEAD /index.html HTTP/1.1\r\n" +
                     "Host: example.com\r\n" +
                     "\r\n";
        
        HttpRequest request = parse(raw);
        
        assertEquals(HttpMethod.HEAD, request.method());
        assertEquals("/index.html", request.path());
    }
    
    // ==================== HEADER TESTS ====================
    
    @Test
    @DisplayName("Should parse multiple headers")
    void shouldParseMultipleHeaders() throws IOException {
        String raw = "GET / HTTP/1.1\r\n" +
                     "Host: example.com\r\n" +
                     "User-Agent: Mozilla/5.0\r\n" +
                     "Accept: text/html\r\n" +
                     "Accept-Language: en-US\r\n" +
                     "Accept-Encoding: gzip\r\n" +
                     "Connection: keep-alive\r\n" +
                     "\r\n";
        
        HttpRequest request = parse(raw);
        
        Map<String, String> headers = request.headers();
        assertEquals(6, headers.size());
        assertEquals("example.com", headers.get("Host"));
        assertEquals("Mozilla/5.0", headers.get("User-Agent"));
        assertEquals("text/html", headers.get("Accept"));
        assertEquals("en-US", headers.get("Accept-Language"));
        assertEquals("gzip", headers.get("Accept-Encoding"));
        assertEquals("keep-alive", headers.get("Connection"));
    }
    
    @Test
    @DisplayName("Should handle headers with extra spaces")
    void shouldHandleHeadersWithSpaces() throws IOException {
        String raw = "GET / HTTP/1.1\r\n" +
                     "Host:    localhost    \r\n" +
                     "Content-Type:  text/plain  \r\n" +
                     "\r\n";
        
        HttpRequest request = parse(raw);
        
        assertEquals("localhost", request.headers().get("Host"));
        assertEquals("text/plain", request.headers().get("Content-Type"));
    }
    
    // ==================== EDGE CASES ====================
    
    @Test
    @DisplayName("Should handle root path")
    void shouldHandleRootPath() throws IOException {
        String raw = "GET / HTTP/1.1\r\n" +
                     "Host: localhost\r\n" +
                     "\r\n";
        
        HttpRequest request = parse(raw);
        
        assertEquals("/", request.path());
    }
    
    @Test
    @DisplayName("Should handle path with special characters")
    void shouldHandleSpecialCharactersInPath() throws IOException {
        String raw = "GET /api/users?name=John%20Doe&age=30 HTTP/1.1\r\n" +
                     "Host: localhost\r\n" +
                     "\r\n";
        
        HttpRequest request = parse(raw);
        
        assertEquals("/api/users?name=John%20Doe&age=30", request.path());
    }
    
    @Test
    @DisplayName("Should handle large body")
    void shouldHandleLargeBody() throws IOException {
        String body = "x".repeat(10000);
        String raw = "POST /api/upload HTTP/1.1\r\n" +
                     "Host: localhost\r\n" +
                     "Content-Length: 10000\r\n" +
                     "\r\n" +
                     body;
        
        HttpRequest request = parse(raw);
        
        assertEquals(10000, request.body().length());
        assertEquals(body, request.body());
    }
    
    // ==================== VALIDATION TESTS ====================
    
    @Test
    @DisplayName("Should reject missing Host header")
    void shouldRejectMissingHost() {
        String raw = "GET /index.html HTTP/1.1\r\n" +
                     "\r\n";
        
        assertThrows(IllegalArgumentException.class, () -> parse(raw));
    }
    
    @Test
    @DisplayName("Should reject invalid Content-Length")
    void shouldRejectInvalidContentLength() {
        String raw = "POST /api HTTP/1.1\r\n" +
                     "Host: localhost\r\n" +
                     "Content-Length: abc\r\n" +
                     "\r\n";
        
        assertThrows(IllegalArgumentException.class, () -> parse(raw));
    }
    
    @Test
    @DisplayName("Should reject malformed request line")
    void shouldRejectMalformedRequestLine() {
        String raw = "GET /index.html\r\n" +
                     "Host: localhost\r\n" +
                     "\r\n";
        
        assertThrows(IOException.class, () -> parse(raw));
    }
    
    @Test
    @DisplayName("Should reject empty request")
    void shouldRejectEmptyRequest() {
        String raw = "";
        
        assertThrows(IOException.class, () -> parse(raw));
    }
    
    // ==================== HTTP VERSION TESTS ====================
    
    @Test
    @DisplayName("Should support HTTP/1.0 without Host header")
    void shouldSupportHttp10() throws IOException {
        String raw = "GET /index.html HTTP/1.0\r\n" +
                     "\r\n";
        
        HttpRequest request = parse(raw);
        
        assertEquals("HTTP/1.0", request.version());
        assertNull(request.headers().get("Host")); // HTTP/1.0 doesn't require Host
    }
    
    @Test
    @DisplayName("Should reject unsupported HTTP version")
    void shouldRejectUnsupportedHttpVersion() {
        String raw = "GET /index.html HTTP/2.0\r\n" +
                     "Host: localhost\r\n" +
                     "\r\n";
        
        assertThrows(IllegalArgumentException.class, () -> parse(raw));
    }
}
