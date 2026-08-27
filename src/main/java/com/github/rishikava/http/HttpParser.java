package com.github.rishikava.http;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

import com.github.rishikava.exceptions.BadRequestException;
import com.github.rishikava.exceptions.RequestTooLargeException;

public class HttpParser {
    private int maxLineSize;
    private int maxHeaderCount;
    private int maxBodySize;

    public HttpParser() {
        this.maxLineSize = 8000;
        this.maxHeaderCount = 100;
        this.maxBodySize = 1000000;
    }

    public HttpRequest parseRequest(BufferedInputStream in) throws IOException {
        // Request line
        String requestLine = readLine(in);
        String[] parts = requestLine.split(" ", 3);

        if (parts.length != 3) {
            throw new BadRequestException("Malformed request line");
        }

        HttpMethod method;
        try {
            method = HttpMethod.valueOf(parts[0]);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid method");
        }

        String path = parts[1];
        String version = parts[2];

        // Headers
        Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        while (true) {
            String line = readLine(in);

            if (line.isEmpty()) {
                break;
            }

            String[] headerParts = line.split(":", 2);

            if (headerParts.length != 2) {
                throw new BadRequestException("Malformed header: " + line);
            }

            String name = headerParts[0].trim();
            String value = headerParts[1].trim();

            headers.put(name, value);

            if (headers.size() > this.maxHeaderCount) {
                throw new RequestTooLargeException("Maximum number of headers exceeded");
            }
        }

        // Body
        String body = null;
        if (headers.containsKey("Content-Length")) {
            int length;
            try {
                length = Integer.parseInt(headers.get("Content-Length"));
            } catch (NumberFormatException e) {
                throw new BadRequestException("Failed parsing Content-Length header");
            }
            if (length > this.maxBodySize) {
                throw new RequestTooLargeException("Request too large: " + length);
            }
            byte[] bodyBytes = readExactly(in, length);
            body = new String(bodyBytes, StandardCharsets.UTF_8);
        }

        return new HttpRequest(method, path, version, headers, body);
    }

	private String readLine(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int previous = -1;

        while (true) {
            int current = in.read();

            if (current == -1) {
                throw new BadRequestException("EOF");
            }

            // Found \r\n
            if (previous == '\r' && current == '\n') {
                break;
            }

            if (previous != -1) {
                buffer.write(previous);
            }

            if (buffer.size() > this.maxLineSize) {
                throw new RequestTooLargeException("Maximum line size exceeded");
            }

            previous = current;
        }

        return buffer.toString(StandardCharsets.US_ASCII);
	}

    private byte[] readExactly(InputStream in, int length) throws IOException {
        if (length < 0) {
            throw new BadRequestException("Invalid Content-Length: " + length);
        }

        byte[] buffer = new byte[length];

        int offset = 0;

        while (offset < length) {
            int read = in.read(buffer, offset, length - offset);

            if (read == -1) {
                throw new BadRequestException("EOF");
            }

            offset += read;
        }

        return buffer;
    }

    public void setMaxLineSize(int maxLineSize) {
        this.maxLineSize = maxLineSize;
    }

    public void setMaxHeaderCount(int maxHeaderCount) {
        this.maxHeaderCount = maxHeaderCount;
    }

    public void setMaxBodySize(int maxBodySize) {
        this.maxBodySize = maxBodySize;
    }
}
