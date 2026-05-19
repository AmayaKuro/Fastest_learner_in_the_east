package com.http_server;

@FunctionalInterface
public interface Handler {
    void handle(ServerHandler ctx) throws Exception;
}
