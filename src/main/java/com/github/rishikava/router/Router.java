package com.github.rishikava.router;

import com.github.rishikava.http.HttpResponse;
import com.github.rishikava.http.HttpMethod;
import com.github.rishikava.http.HttpRequest;

import com.github.rishikava.handler.DefaultHandler;
import com.github.rishikava.handler.Handler;

public class Router {
    private RouterTree routes;

    public Router() {
        this.routes = new RouterTree();
    }

    public HttpResponse route(HttpRequest request) {
        Handler handler = routes.resolveRoute(request.method(), request.path());
        if (handler == null) {
            handler = new DefaultHandler();
        }

        return handler.handle(request);
    }

    public void register(HttpMethod method, String route, Handler handler) {
        routes.registerRoute(method, route, handler);
    }

    public RouterTree getRoutes() {
        return routes;
    }
}
