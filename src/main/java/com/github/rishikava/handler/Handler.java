package com.github.rishikava.handler;

import com.github.rishikava.http.HttpResponse;
import com.github.rishikava.http.HttpRequest;

public interface Handler {
    HttpResponse handle(HttpRequest request);
}
