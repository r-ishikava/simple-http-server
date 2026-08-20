package com.github.rishikava.router;

import com.github.rishikava.http.HttpResponse;
import com.github.rishikava.http.HttpRequest;

import java.util.HashMap;
import java.util.Map;

import com.github.rishikava.handler.DefaultHandler;
import com.github.rishikava.handler.Handler;

public class Router {
    private Map<Route, Handler> routes;

    public Router() {
        this.routes = new HashMap<>();
    }

    public HttpResponse route(HttpRequest request) {
        Route route = new Route(request.method(), request.path());
        Handler handler = routes.get(route);
        if (handler == null) {
            handler = new DefaultHandler();
        }

        return handler.handle(request);
    }

    public void register(Route route, Handler handler) {
        routes.put(route, handler);
    }
}
