package com.github.rishikava.server;

import com.github.rishikava.router.*;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Config {
    private final int port;
    private final String host;
    private final int requestTimeout;
    private final int maxLineSize;
    private final int maxHeaderCount;
    private final int maxBodySize;
    private final Path staticDirectory;
    private final int backlog;
    private final int maxThreads;
    private final Router router;

    private Config(Builder builder) {
        this.port = builder.port;
        this.host = builder.host;
        this.requestTimeout = builder.requestTimeout;
        this.maxLineSize = builder.maxLineSize;
        this.maxHeaderCount = builder.maxHeaderCount;
        this.maxBodySize = builder.maxBodySize;
        this.staticDirectory = builder.staticDirectory;
        this.backlog = builder.backlog;
        this.maxThreads = builder.maxThreads;
        this.router = builder.router;
    }

    public static class Builder {
        // Required fields
        private Router router;

        // Fields with defaults
        private int port = 8080;
        private String host = "localhost";
        private int requestTimeout = 10000;
        private int maxLineSize = 8000;
        private int maxHeaderCount = 100;
        private int maxBodySize = 1000000;
        private Path staticDirectory = Paths.get(System.getProperty("user.dir") + "/static");
        private int backlog = 50;
        private int maxThreads = 10;

        public Builder router(Router router) {
            this.router = router;
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

        public Builder maxLineSize(int maxLineSize) {
            this.maxLineSize = maxLineSize;
            return this;
        }
        
        public Builder maxHeaderCount(int maxHeaderCount) {
            this.maxHeaderCount = maxHeaderCount;
            return this;
        }
        
        public Builder maxBodySize(int maxBodySize) {
            this.maxBodySize = maxBodySize;
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

    public int getMaxLineSize() {
        return maxLineSize;
    }

    public int getMaxHeaderCount() {
        return maxHeaderCount;
    }

    public int getMaxBodySize() {
        return maxBodySize;
    }

	public Path getStaticDirectory() {
		return staticDirectory;
	}

	public Router getRouter() {
		return router;
	}

	public int getBacklog() {
		return backlog;
	}

    public int getMaxThreads() {
        return maxThreads;
    }
}
