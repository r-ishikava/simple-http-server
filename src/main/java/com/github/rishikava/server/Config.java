package com.github.rishikava.server;

import com.github.rishikava.http.*;
import com.github.rishikava.router.*;
import com.github.rishikava.util.HttpSerializer;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Config {
    private final int port;
    private final String host;
    private final int requestTimeout;
    private final int maxRequestSize;
    private final Path staticDirectory;
    private final int backlog;
    private final int maxThreads;
    private final Router router;
    private final HttpParser parser;
    private final HttpSerializer serializer;

    private Config(Builder builder) {
        this.port = builder.port;
        this.host = builder.host;
        this.requestTimeout = builder.requestTimeout;
        this.maxRequestSize = builder.maxRequestSize;
        this.staticDirectory = builder.staticDirectory;
        this.backlog = builder.backlog;
        this.maxThreads = builder.maxThreads;
        this.router = builder.router;
        this.parser = builder.parser;
        this.serializer = builder.serializer;
    }

    public static class Builder {
        // Required fields
        private Router router;
        private HttpParser parser;
        private HttpSerializer serializer;

        // Fields with defaults
        private int port = 8080;
        private String host = "localhost";
        private int requestTimeout = 10000;
        private int maxRequestSize = 1000000;
        private Path staticDirectory = Paths.get(System.getProperty("user.dir") + "/static");
        private int backlog = 50;
        private int maxThreads = 10;

        public Builder router(Router router) {
            this.router = router;
            return this;
        }

        public Builder parser(HttpParser parser) {
            this.parser = parser;
            return this;
        }

        public Builder serializer(HttpSerializer serializer) {
            this.serializer = serializer;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder requestTimeout(int requestTimeout) {
            this.requestTimeout = requestTimeout;
            return this;
        }

        public Builder maxRequestSize(int maxRequestSize) {
            this.maxRequestSize = maxRequestSize;
            return this;
        }

        public Builder staticDirectory(Path staticDirectory) {
            this.staticDirectory = staticDirectory;
            return this;
        }

        public Builder backlog(int backlog) {
            this.backlog = backlog;
            return this;
        }

        public Builder maxThreads(int maxThreads) {
            this.maxThreads = maxThreads;
            return this;
        }

        public Config build() {
            if (router == null) {
                throw new IllegalArgumentException("Router is required");
            }

            if (parser == null) {
                throw new IllegalArgumentException("Parser is required");
            }

            if (serializer == null) {
                throw new IllegalArgumentException("Http serializer is required");
            }

            return new Config(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

	public int getPort() {
		return port;
	}

	public String getHost() {
		return host;
	}

	public int getRequestTimeout() {
		return requestTimeout;
	}

	public int getMaxRequestSize() {
		return maxRequestSize;
	}

	public Path getStaticDirectory() {
		return staticDirectory;
	}

	public Router getRouter() {
		return router;
	}

	public HttpParser getParser() {
		return parser;
	}

	public HttpSerializer getSerializer() {
		return serializer;
	}

	public int getBacklog() {
		return backlog;
	}

    public int getMaxThreads() {
        return maxThreads;
    }
}
