package com.example.pastebin.controller;


import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    private final StringRedisTemplate redis;

    public HealthController(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @GetMapping("/healthz")
    public Map<String, Boolean> health() {
        try {
            redis.opsForValue().get("health");
            return Map.of("ok", true);
        } catch (Exception e) {
            return Map.of("ok", false);
        }
    }
}

