package com.http_server.Utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class BufferInputStream {
    public static String readLine(BufferedInputStream stream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int b;
        int prev = -1;
        
        while ((b = stream.read()) != -1) {
            // Check for \r\n boundary
            if (prev == '\r' && b == '\n') {
                byte[] bytes = buffer.toByteArray();
                // Convert buffer to String, excluding the trailing \r (length - 1)
                return new String(bytes, 0, bytes.length - 1, StandardCharsets.UTF_8);
            }
            buffer.write(b);
            prev = b;
        }
        
        // Handle EOF without trailing newline
        return buffer.size() > 0 ? buffer.toString(StandardCharsets.UTF_8) : "";
    }
}
