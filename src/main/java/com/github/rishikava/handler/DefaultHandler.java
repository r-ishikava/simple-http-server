package com.github.rishikava.handler;

import com.github.rishikava.http.HttpRequest;
import com.github.rishikava.http.HttpResponse;

public class DefaultHandler implements Handler {
	@Override
	public HttpResponse handle(HttpRequest request) {
        return HttpResponse.notFound(request.version());
	}

}
