package com.http_server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import com.http_server.Model.RequestData;
import com.http_server.Model.ResponseData;

public class ServerHandler {
    public Socket socket;
    public RequestData request;
    public ResponseData response;
    public Map<String, Handler> routes = new HashMap<>();

    public ServerHandler(Socket socket) {
        this.socket = socket;
    }

    public void handleRequest() {
        try {
            request = parseRequest();
            response = buildDefaultResponse();

            String routeKey = request.method + " " + request.url;
            Handler handler = routes.get(routeKey);

            if (handler != null) {
                handler.handle(this);
            } else {
                notFound();
            }

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public RequestData parseRequest() throws Exception {
        RequestData requestData = new RequestData();
        BufferedReader input = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        
        Callable<Void> parseTask = () -> {
            parseRequestLine(input, requestData);
            parseHeaders(input, requestData);
            parseBody(input, requestData);
            return null;
        };

        try {
            Future<Void> future = executor.submit(parseTask);
            future.get(10, java.util.concurrent.TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            e.printStackTrace();
            throw new IOException("Time out");
        } finally {
            executor.shutdownNow();
        }

        System.out.println("Client says: " + requestData);
        return requestData;
    }

    public void parseRequestLine(BufferedReader reader, RequestData requestData) throws IOException {
        String requestLine = reader.readLine();

        var parts = requestLine.split(" ");
        requestData.method = parts[0];
        requestData.url = parts[1];
        requestData.protocol = parts[2];
    }

    public void parseHeaders(BufferedReader reader, RequestData requestData) throws IOException {
        for (String line; !(line = reader.readLine()).isEmpty();) {
            var headerParts = line.split(":");
            var headerName = headerParts[0].toLowerCase();
            var headerValue = headerParts[1].trim();
            requestData.headers.put(headerName, headerValue);
        }
    }

    public void parseBody(BufferedReader reader, RequestData requestData) throws IOException {

        if (requestData.headers.containsKey("content-length")) {
            try {
                var contentLength = Integer.parseInt(requestData.headers.get("content-length"));
                var body = reader.readLine().substring(0, contentLength);
                requestData.body = body.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            } catch (NumberFormatException e) {
                requestData.body = new byte[0];
            }
        } else if (requestData.headers.containsKey("transfer-encoding")) {
            // Placeholder for actual chunked parsing logic
            requestData.body = reader.readLine().getBytes(java.nio.charset.StandardCharsets.UTF_8); //
        } else {
            requestData.body = new byte[0];
        }
    }

    public ResponseData buildDefaultResponse() {
        var response = new ResponseData();
        response.protocol = request.protocol;
        response.statusCode = 200;
        response.statusMessage = "OK";
        response.headers = new java.util.HashMap<>(Map.of(
                "Server", "http_java",
                "Content-Type", "text/plain",
                "Content-Length", "0"));
        response.body = "Hello, from server!".getBytes(java.nio.charset.StandardCharsets.UTF_8);

        return response;
    }

    public void ok() {
        handleResponse(200, "OK", null);
    }

    public void ok(Object body) {
        handleResponse(200, "OK", body);
    }

    public void created() {
        handleResponse(201, "Created", null);
    }

    public void created(Object body) {
        handleResponse(201, "Created", body);
    }

    public void accepted() {
        handleResponse(202, "Accepted", null);
    }

    public void accepted(Object body) {
        handleResponse(202, "Accepted", body);
    }

    public void badRequest() {
        handleResponse(400, "Bad Request", null);
    }

    public void badRequest(Object body) {
        handleResponse(400, "Bad Request", body);
    }

    public void notAllowed() {
        handleResponse(405, "Method Not Allowed", null);
    }

    public void notAllowed(Object body) {
        handleResponse(405, "Method Not Allowed", body);
    }

    public void notFound() {
        handleResponse(404, "Not Found", null);
    }

    public void notFound(Object body) {
        handleResponse(404, "Not Found", body);
    }

    public void internalServerError() {
        handleResponse(500, "Internal Server Error", null);
    }

    public void internalServerError(Object body) {
        handleResponse(500, "Internal Server Error", body);
    }

    private void handleResponse(int statusCode, String statusMessage, Object bodyObj) {
        response.statusCode = statusCode;
        response.statusMessage = statusMessage;

        switch (bodyObj) {
            case null: {
                response.headers.put("Content-Length", "0");
                break;
            }
            case String s: {
                byte[] bytes = s.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                response.headers.put("Content-Length", String.valueOf(bytes.length));
                response.headers.put("Content-Type", "text/plain");
                response.body = bytes;
                break;
            }
            case byte[] b: {
                response.headers.put("Content-Length", String.valueOf(b.length));
                response.headers.put("Content-Type", "application/octet-stream");
                response.body = b;
                break;
            }
            case Object obj: {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    var bytes = mapper.writeValueAsBytes(obj);
                    response.headers.put("Content-Length", String.valueOf(bytes.length));
                    response.headers.put("Content-Type", "application/json");
                    response.body = bytes;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }

        // For HEAD requests, we should not include the body in the response
        if (request.method.equals("HEAD")) {
            response.body = null;
        }
        sendResponse();
    }

    private void sendResponse() {
        try {
            java.io.OutputStream out = socket.getOutputStream();
            PrintWriter output = new PrintWriter(out, true);

            output.print(response.protocol + " " + response.statusCode + " " + response.statusMessage + "\r\n");
            for (var header : response.headers.entrySet()) {
                output.print(header.getKey() + ": " + header.getValue() + "\r\n");
            }
            output.print("\r\n");
            output.flush();

            if (response.body != null && response.body.length > 0) {
                out.write(response.body);
                out.flush();
            }

            // What to do after sending the response?
            // HTTP/1.1 allows for persistent connections
            // does we send EOF now?
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void use(String method, String path, Handler handler) {
        routes.put(method.toUpperCase() + " " + path, handler);
    }
}
