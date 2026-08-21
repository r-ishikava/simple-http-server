package com.github.rishikava.handler;

import java.util.HashMap;

import com.github.rishikava.http.HttpRequest;
import com.github.rishikava.http.HttpResponse;

public class EchoHandler implements Handler {
    // Assumes request is well formed.
    // If there is a body then the correct Content-Length is 
    // present.
	@Override
	public HttpResponse handle(HttpRequest request) {
        String contentLength = request.headers().get("Content-Length");

        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "text/plain");
        if (contentLength != null) {
            headers.put("Content-Length", contentLength);
        }

        return new HttpResponse(
            request.version(),
            200,
            "OK",
            headers,
            request.body().getBytes()
        );
	}
}
