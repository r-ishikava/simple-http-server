package com.github.rishikava.handler;

import com.github.rishikava.http.HttpRequest;
import com.github.rishikava.http.HttpResponse;

public class SleepHandler implements Handler {
    @Override
    public HttpResponse handle(HttpRequest request) {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new HttpResponse(request.version(), 200, null, null, null);
    }
}
