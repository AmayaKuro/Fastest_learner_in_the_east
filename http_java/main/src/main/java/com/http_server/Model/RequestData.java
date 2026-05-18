package com.http_server.Model;

import java.util.HashMap;
import java.util.Map;

public class RequestData {
    public String method;
    public String url;
    public String protocol;
    public Map<String, String> headers = new HashMap<>();
    public byte[] body;

    @Override
    public String toString() {
        return "RequestData{" +
                "method='" + method + '\'' +
                ", url='" + url + '\'' +
                ", protocol='" + protocol + '\'' +
                ", headers=" + headers +
                // ", body=" + new String(body) +
                '}';
    }
}
