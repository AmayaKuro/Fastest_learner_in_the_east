package com.http_server.Model;

public class RequestData {
    public String method;
    public String url;
    public String protocol;
    public Map<String, String> headers = new HashMap<>();
    public String body;
}
