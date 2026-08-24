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
        return HttpResponse.ok(request.version(), null, new byte[0]);
    }
}
