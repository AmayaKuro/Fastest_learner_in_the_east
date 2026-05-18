package com.http_server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;

import com.http_server.Model.RequestData;

public class ServerHandler {
    public Socket socket;
    public RequestData request;
    // TODO: Build responseData so that it can be used to generate the response
    public ResponseData response;

    public ServerHandler(Socket socket) {
        this.socket = socket;
    }

    public void handleRequest() {
        try {
            request = parseRequest();
            response = buildDefaultResponse();

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Further processing of requestData
    }

    public RequestData parseRequest() throws Exception {
        var requestData = new RequestData();
        BufferedReader input = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
        String data = input.readAllAsString();

        var lines = data.split("\r\n\n\r");
        var headerLines = lines[0].split("\r\n");

        parseRequestLine(headerLines[0], requestData);
        parseHeaders(headerLines, requestData);
        parseBody(lines[1], requestData);

        System.out.println("Client says: " + data);
        return requestData;
    }

    public void parseRequestLine(String requestLine, RequestData requestData) {
        var parts = requestLine.split(" ");
        requestData.method = parts[0];
        requestData.url = parts[1];
        requestData.protocol = parts[2];
    }

    public void parseHeaders(String[] headerLines, RequestData requestData) {
        for (int i = 1; i < headerLines.length; i++) {
            var headerParts = headerLines[i].split(":");
            var headerName = headerParts[0].toLowerCase();
            var headerValue = headerParts[1].trim();
            requestData.headers.put(headerName, headerValue);
        }
    }

    public void parseBody(String raw, RequestData requestData) {
        if (requestData.headers.containsKey("content-length")) {
            try {
                var contentLength = Integer.parseInt(requestData.headers.get("content-length"));
                requestData.body = raw.substring(0, contentLength);
            } catch (NumberFormatException e) {
                requestData.body = "";
            }
        } else if (requestData.headers.containsKey("transfer-encoding")) {
            // Handle chunked transfer encoding
            requestData.body = raw; // Placeholder for actual chunked parsing logic
        } else {
            requestData.body = "";
        }
    }

    public ResponseData buildDefaultResponse() {
        var response = new ResponseData();
        response.protocol = request.protocol;
        response.statusCode = 200;
        response.statusMessage = "OK";
        response.headers = new java.util.HashMap<>(Map.of(
                "Content-Type", "text/plain",
                "Content-Length", "0"));
        response.body = "Hello, from server!".getBytes(java.nio.charset.StandardCharsets.UTF_8);

        return response;
    }

    public void ok(Object body) {
        handleResponse(200, "OK", body);
    }

    public void created(Object body) {
        handleResponse(201, "Created", body);
    }

    public void accepted(Object body) {
        handleResponse(202, "Accepted", body);
    }

    public void badRequest(Object body) {
        handleResponse(400, "Bad Request", body);
    }

    public void notAllowed(Object body) {
        handleResponse(405, "Method Not Allowed", body);
    }

    public void notFound(Object body) {
        handleResponse(404, "Not Found", body);
    }

    public void internalServerError(Object body) {
        handleResponse(500, "Internal Server Error", body);
    }

    private void handleResponse(int statusCode, String statusMessage, Object bodyObj) {
        response.statusCode = statusCode;
        response.statusMessage = statusMessage;

        if (bodyObj == null) {
            response.headers.put("Content-Length", "0");
        }

        switch (bodyObj) {
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
                StringBuilder jsonBuilder = new StringBuilder();
                jsonBuilder.append("{");
                java.lang.reflect.Field[] fields = obj.getClass().getDeclaredFields();
                for (int i = 0; i < fields.length; i++) {
                    fields[i].setAccessible(true);
                    jsonBuilder.append("\"").append(fields[i].getName()).append("\":");
                    try {
                        Object value = fields[i].get(obj);
                        if (value instanceof String) {
                            jsonBuilder.append("\"").append(value).append("\"");
                        } else {
                            jsonBuilder.append(value);
                        }
                    } catch (IllegalAccessException e) {
                        jsonBuilder.append("null");
                    }
                    if (i < fields.length - 1) {
                        jsonBuilder.append(",");
                    }
                }
                jsonBuilder.append("}");
                byte[] bytes = jsonBuilder.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
                response.headers.put("Content-Length", String.valueOf(bytes.length));
                response.headers.put("Content-Type", "application/json");
                response.body = bytes;
                break;
            }
            default:
                break;
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
}
