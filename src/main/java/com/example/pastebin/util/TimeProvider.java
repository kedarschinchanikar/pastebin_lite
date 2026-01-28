package com.example.pastebin.util;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TimeProvider {

    @Value("${TEST_MODE:0}")
    private String testMode;

    public long now(HttpServletRequest request) {
        if ("1".equals(testMode)) {
            String header = request.getHeader("x-test-now-ms");
            if (header != null) {
                return Long.parseLong(header);
            }
        }
        return System.currentTimeMillis();
    }
}