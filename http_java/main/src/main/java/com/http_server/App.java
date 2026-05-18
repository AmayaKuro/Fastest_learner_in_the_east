package com.http_server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class App {
    public static void main(String[] args) throws Exception {
        int port = 8080;


        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started. Waiting for client...");
           
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client connected!");

                new Thread(() -> {
                    // Handle the client request
                    ServerHandler handler = new ServerHandler(socket);
                    handler.handleRequest();
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Server stopped.");
    }
}
