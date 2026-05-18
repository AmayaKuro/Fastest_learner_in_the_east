package com.http_server.Model;

public class ResponseData {
    public String protocol;
    public int statusCode;
    public String statusMessage;
    public Map<String, String> headers = new HashMap<>();
    public String body;
}
