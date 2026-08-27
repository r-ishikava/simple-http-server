package com.github.rishikava.server;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.rishikava.handler.EchoHandler;
import com.github.rishikava.http.HttpMethod;
import com.github.rishikava.http.HttpRequest;
import com.github.rishikava.router.Route;
import com.github.rishikava.router.Router;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class KeepAliveTest {
    private static final int SERVER_READ_TIMEOUT_MS = 800;
    private static final int TEST_READ_TIMEOUT_MS = 5000;

    private Server server;
    private ServerSocket bootstrap;
    private Socket client;
    private Socket serverSide;
    private ExecutorService executor;

    @BeforeEach
    void setUp() throws IOException {
        Router router = new Router();
        router.register(new Route(HttpMethod.POST, "/echo"), new EchoHandler());

        Config config = Config.builder()
            .router(router)
            .build();

        server = new Server(config);
        executor = Executors.newSingleThreadExecutor();

        // In-process socket pair so handleClient can be driven without run()
        bootstrap = new ServerSocket(0);
        client = new Socket("localhost", bootstrap.getLocalPort());
        client.setSoTimeout(TEST_READ_TIMEOUT_MS);
        serverSide = bootstrap.accept();
        serverSide.setSoTimeout(SERVER_READ_TIMEOUT_MS); // mirrors what Server.run() does
    }

    @AfterEach
    void tearDown() throws IOException {
        if (executor != null) {
            executor.shutdownNow();
        }
        closeQuietly(client);
        closeQuietly(serverSide);
        closeQuietly(bootstrap);
    }

    // ==================== KEEP-ALIVE DECISION ====================

    @Test
    @DisplayName("HTTP/1.1 defaults to keep-alive when Connection header is absent")
    void http11DefaultsToKeepAlive() {
        assertTrue(server.shouldKeepAlive(request("HTTP/1.1", null)));
    }

    @Test
    @DisplayName("HTTP/1.1 with Connection: close must close")
    void http11ConnectionClose() {
        assertFalse(server.shouldKeepAlive(request("HTTP/1.1", "close")));
    }

    @Test
    @DisplayName("HTTP/1.1 with explicit keep-alive stays alive")
    void http11ExplicitKeepAlive() {
        assertTrue(server.shouldKeepAlive(request("HTTP/1.1", "keep-alive")));
    }

    @Test
    @DisplayName("HTTP/1.0 defaults to close when Connection header is absent")
    void http10DefaultsToClose() {
        assertFalse(server.shouldKeepAlive(request("HTTP/1.0", null)));
    }

    @Test
    @DisplayName("HTTP/1.0 with Connection: keep-alive upgrades to persistent")
    void http10KeepAliveUpgrade() {
        assertTrue(server.shouldKeepAlive(request("HTTP/1.0", "keep-alive")));
    }

    @Test
    @DisplayName("HTTP/1.0 with Connection: close closes")
    void http10ConnectionClose() {
        assertFalse(server.shouldKeepAlive(request("HTTP/1.0", "close")));
    }

    @Test
    @DisplayName("'close' token wins when Connection lists multiple tokens")
    void closeTokenTakesPrecedence() {
        assertFalse(server.shouldKeepAlive(request("HTTP/1.1", "keep-alive, close")));
    }

    // ==================== PERSISTENT CONNECTION LOOP ====================

    @Test
    @DisplayName("Serves two sequential requests on one connection")
    void servesTwoRequestsOnSameConnection() throws IOException {
        executor.submit(() -> server.handleClient(serverSide));

        writeRequest(client.getOutputStream(), postEcho("first"));
        ParsedResponse first = readResponse(client.getInputStream());
        assertEquals(200, first.code());
        assertEquals("first", first.body());

        writeRequest(client.getOutputStream(), get("HTTP/1.1", "close"));
        ParsedResponse second = readResponse(client.getInputStream());
        assertEquals(404, second.code());

        assertEquals(-1, client.getInputStream().read(), "Server should close after Connection: close");
    }

    @Test
    @DisplayName("HTTP/1.0 connection persists only with Connection: keep-alive")
    void http10PersistsWhenUpgraded() throws IOException {
        executor.submit(() -> server.handleClient(serverSide));

        writeRequest(client.getOutputStream(), get("HTTP/1.0", "keep-alive"));
        assertEquals(404, readResponse(client.getInputStream()).code());

        writeRequest(client.getOutputStream(), get("HTTP/1.0", "close"));
        assertEquals(404, readResponse(client.getInputStream()).code());

        assertEquals(-1, client.getInputStream().read(), "Server should close after Connection: close");
    }

    @Test
    @DisplayName("Malformed request on a persistent connection returns 400 and closes")
    void malformedSecondRequestReturns400AndCloses() throws IOException {
        executor.submit(() -> server.handleClient(serverSide));

        writeRequest(client.getOutputStream(), get("HTTP/1.1", null));
        assertEquals(404, readResponse(client.getInputStream()).code());

        writeRequest(client.getOutputStream(), "NONSENSE\r\n\r\n");
        ParsedResponse error = readResponse(client.getInputStream());
        assertEquals(400, error.code());

        assertEquals(-1, client.getInputStream().read(), "Server should not trust framing after a parse failure");
    }

    @Test
    @DisplayName("Idle connection is closed after the read timeout elapses")
    void idleConnectionTimesOut() throws IOException {
        executor.submit(() -> server.handleClient(serverSide));

        writeRequest(client.getOutputStream(), get("HTTP/1.1", null));
        assertEquals(404, readResponse(client.getInputStream()).code());

        // Send nothing further: server's SO_TIMEOUT fires and it closes the socket.
        assertEquals(-1, client.getInputStream().read(), "Server should close an idle connection");
    }

    @Test
    @DisplayName("Server honors Connection: close regardless of header casing")
    void connectionHeaderCaseInsensitive() throws IOException {
        // Idle timeout longer than the client's: only an honored Connection
        // header can close the socket before the client's own read times out.
        serverSide.setSoTimeout(TEST_READ_TIMEOUT_MS * 6);

        executor.submit(() -> server.handleClient(serverSide));

        writeRequest(client.getOutputStream(),
            "GET /missing HTTP/1.1\r\nHost: localhost\r\ncOnNeCtIoN: cLoSe\r\n\r\n");

        assertEquals(404, readResponse(client.getInputStream()).code());
        assertEquals(-1, client.getInputStream().read(), "Mixed-case Connection header must still close");
    }

    // ==================== HELPERS ====================

    private static HttpRequest request(String version, String connectionValue) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Host", "localhost");
        if (connectionValue != null) {
            headers.put("Connection", connectionValue);
        }
        return new HttpRequest(HttpMethod.GET, "/", version, headers, null);
    }

    private static String get(String version, String connectionValue) {
        StringBuilder sb = new StringBuilder();
        sb.append("GET /missing ").append(version).append("\r\nHost: localhost\r\n");
        if (connectionValue != null) {
            sb.append("Connection: ").append(connectionValue).append("\r\n");
        }
        sb.append("\r\n");
        return sb.toString();
    }

    private static String postEcho(String body) {
        return "POST /echo HTTP/1.1\r\n" +
               "Host: localhost\r\n" +
               "Content-Type: text/plain\r\n" +
               "Content-Length: " + body.length() + "\r\n" +
               "\r\n" +
               body;
    }

    private static void writeRequest(OutputStream out, String raw) throws IOException {
        out.write(raw.getBytes(StandardCharsets.US_ASCII));
        out.flush();
    }

    /**
     * Reads exactly one response off the wire using Content-Length framing,
     * so byte offsets stay correct for the next response on the connection.
     */
    private static ParsedResponse readResponse(InputStream in) throws IOException {
        ByteArrayOutputStream head = new ByteArrayOutputStream();
        while (!endsWithHeaderTerminator(head)) {
            int b = in.read();
            if (b == -1) {
                throw new AssertionError("Connection closed before the full response header arrived");
            }
            head.write(b);
        }

        String[] lines = head.toString(StandardCharsets.US_ASCII).split("\r\n");
        String[] statusParts = lines[0].split(" ", 3);

        Map<String, String> headers = new HashMap<>();
        for (int i = 1; i < lines.length; i++) {
            String[] kv = lines[i].split(":", 2);
            headers.put(kv[0].trim(), kv[1].trim());
        }

        int contentLength = headers.containsKey("Content-Length")
                ? Integer.parseInt(headers.get("Content-Length"))
                : 0;

        byte[] body = new byte[contentLength];
        int offset = 0;
        while (offset < contentLength) {
            int read = in.read(body, offset, contentLength - offset);
            if (read == -1) {
                throw new AssertionError("Connection closed before the full response body arrived");
            }
            offset += read;
        }

        return new ParsedResponse(
            statusParts[0],
            Integer.parseInt(statusParts[1]),
            statusParts.length > 2 ? statusParts[2] : "",
            headers,
            new String(body, StandardCharsets.UTF_8)
        );
    }

    private static boolean endsWithHeaderTerminator(ByteArrayOutputStream buffer) {
        byte[] bytes = buffer.toByteArray();
        return bytes.length >= 4
            && bytes[bytes.length - 4] == '\r'
            && bytes[bytes.length - 3] == '\n'
            && bytes[bytes.length - 2] == '\r'
            && bytes[bytes.length - 1] == '\n';
    }

    private static void closeQuietly(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private static void closeQuietly(ServerSocket serverSocket) {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private record ParsedResponse(
        String version,
        int code,
        String reason,
        Map<String, String> headers,
        String body
    ) {}
}
