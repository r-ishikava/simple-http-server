package com.github.rishikava;

import com.github.rishikava.server.*;

import java.io.IOException;
import java.nio.file.Path;

import com.github.rishikava.http.*;
import com.github.rishikava.router.*;
import com.github.rishikava.util.HttpSerializer;
import com.github.rishikava.handler.*;

public class Main {
    public static void main(String[] args) {
        Router router = new Router();
        EchoHandler echoHandler = new EchoHandler();
        StaticHandler staticHandler = new StaticHandler(Path.of("/home/rafael/projects/http-server/static"));
        SleepHandler sleepHandler = new SleepHandler();
        router.register(new Route(HttpMethod.POST, "/echo"), echoHandler);
        router.register(new Route(HttpMethod.GET, "/static/image.png"), staticHandler);
        router.register(new Route(HttpMethod.GET, "/static/index.html"), staticHandler);
        router.register(new Route(HttpMethod.GET, "/sleep"), sleepHandler);

        HttpParser parser = new HttpParser();
        HttpSerializer serializer = new HttpSerializer();

        Config config = Config.builder()
            .router(router)
            .parser(parser)
            .serializer(serializer)
            .port(8080)
            .maxThreads(10)
            .build();

        Server server = new Server(config);
        try {
            server.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
