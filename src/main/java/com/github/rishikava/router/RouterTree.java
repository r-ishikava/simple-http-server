package com.github.rishikava.router;

import java.util.HashMap;
import java.util.Map;

import com.github.rishikava.handler.Handler;
import com.github.rishikava.http.HttpMethod;

public class RouterTree {
    private RouteNode root;

    public RouterTree() {
        this.root = new RouteNode();
    }

    public void registerRoute(HttpMethod method, String route, Handler handler) {
        String[] routeParts = route.split("/");
        RouteNode currentNode = this.root;
        for (String part : routeParts) {
            if (part.startsWith("{:") && part.endsWith("}")) {
                RouteNode parameterChild = new RouteNode();
                currentNode.parameterChild = parameterChild;
                currentNode = parameterChild;
            } else {
                if (currentNode.children.containsKey(part)) {
                    currentNode = root.children.get(part);
                    continue;
                } else {
                    RouteNode newNode = new RouteNode();
                    currentNode.children.put(part, newNode);
                    currentNode = newNode;
                }

            }
        }
        currentNode.handlers.putIfAbsent(method, handler);
    }

    public Handler resolveRoute(HttpMethod method, String route) {
        RouteNode currentNode = this.root;
        for (String part : route.split("/")) {
            if (currentNode.children.containsKey(part)) {
                currentNode = currentNode.children.get(part);
            } else if (currentNode.parameterChild != null) {
                currentNode = currentNode.parameterChild;
            } else {
                return null;
            }
        }

        return currentNode.handlers.get(method);
    }

    private class RouteNode {
        private Map<String, RouteNode> children;
        private RouteNode parameterChild;
        private Map<HttpMethod, Handler> handlers;

        public RouteNode() {
            this.children = new HashMap<>();
            this.parameterChild = null;
            this.handlers = new HashMap<>();
        }
    }
}
