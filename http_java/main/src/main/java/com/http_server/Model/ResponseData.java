package com.http_server.Model;

import java.util.HashMap;
import java.util.Map;

public class ResponseData {
    public String protocol;
    public int statusCode;
    public String statusMessage;
    public Map<String, String> headers = new HashMap<>();
    public byte[] body;

    @Override
    public String toString() {
        return "ResponseData{" +
                "protocol='" + protocol + '\'' +
                ", statusCode=" + statusCode +
                ", statusMessage='" + statusMessage + '\'' +
                ", headers=" + headers +
                // ", body=" + new String(body) +
                '}';
    }
}
