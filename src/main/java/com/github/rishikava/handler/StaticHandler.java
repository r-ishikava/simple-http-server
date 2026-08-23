package com.github.rishikava.handler;

import java.nio.file.Path;
import java.util.HashMap;
import java.io.IOException;
import java.nio.file.Files;

import com.github.rishikava.http.HttpRequest;
import com.github.rishikava.http.HttpResponse;
import com.github.rishikava.util.MimeTypes;

// TODO: No error handling
public class StaticHandler implements Handler {
    private Path root;

    public StaticHandler(Path root) {
        try {
            this.root = root.toRealPath();
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid root directory");
        }

        if (!Files.isDirectory(this.root) || !Files.isExecutable(this.root)) {
            throw new IllegalArgumentException("Root directory is either not a directory or is inaccessible");
        }
    }

	@Override
	public HttpResponse handle(HttpRequest request) {
        Path requestedFile = Path.of(request.path());

        try {
            requestedFile = requestedFile.subpath(1, requestedFile.getNameCount());
        } catch (IllegalArgumentException e) {
            requestedFile = Path.of("");
        }

        if (requestedFile.toString().startsWith("/")) {
            requestedFile = this.root.resolve(requestedFile.toString().substring(1)).normalize();
        } else {
            requestedFile = this.root.resolve(requestedFile).normalize();
        }
        try {
			requestedFile = requestedFile.toRealPath();
		} catch (IOException e) {
            return new HttpResponse(request.version(), 404, "File Not Found", null, null);
		}

        if (!requestedFile.startsWith(root) || requestedFile.getFileName().toString().startsWith(".")) {
            return new HttpResponse(request.version(), 404, "File Not Found", null, null);
        }


        String fileName = requestedFile.getFileName().toString();
        String extension = "";

        int dot = fileName.lastIndexOf('.');
        if (dot > 0 && dot < fileName.length() - 1) {
            extension = fileName.substring(dot);
        }

        String mimeType = MimeTypes.getMimeType(extension);
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }

        try {
            byte[] data = Files.readAllBytes(requestedFile);

            HashMap<String, String> headers = new HashMap<>();
            headers.put("Content-Type", mimeType);
            headers.put("Content-Length", String.valueOf(data.length));

            return new HttpResponse(request.version(), 200, "OK", headers, data);
        } catch (IOException e) {
            return new HttpResponse(request.version(), 404, "File Not Found", null, null);
        }
	}
}
