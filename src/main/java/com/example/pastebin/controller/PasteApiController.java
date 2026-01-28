package com.example.pastebin.controller;


import com.example.pastebin.model.Paste;
import com.example.pastebin.util.TimeProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/pastes")
public class PasteApiController {

    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;
    private final TimeProvider timeProvider;

    @Value("${app.base-url}")
    private String baseUrl;

    public PasteApiController(
            StringRedisTemplate redis,
            ObjectMapper mapper,
            TimeProvider timeProvider) {

        this.redis = redis;
        this.mapper = mapper;
        this.timeProvider = timeProvider;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body)
            throws Exception {

        String content = (String) body.get("content");
        Integer ttl = (Integer) body.get("ttl_seconds");
        Integer views = (Integer) body.get("max_views");

        if (content == null || content.trim().isEmpty())
            return bad();

        if ((ttl != null && ttl < 1) || (views != null && views < 1))
            return bad();

        String id = UUID.randomUUID().toString().substring(0, 8);
        String key = "paste:" + id;

        Paste paste = new Paste(
                content,
                ttl == null ? null : System.currentTimeMillis() + ttl * 1000L,
                views
        );

        redis.opsForValue().set(key, mapper.writeValueAsString(paste));
        if (ttl != null)
            redis.expire(key, Duration.ofSeconds(ttl));

        return ResponseEntity.ok(Map.of(
                "id", id,
                "url", baseUrl + "/p/" + id
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> fetch(@PathVariable String id,
                                   HttpServletRequest request)
            throws Exception {

        String key = "paste:" + id;
        String json = redis.opsForValue().get(key);
        if (json == null) return notFound();

        Paste paste = mapper.readValue(json, Paste.class);
        long now = timeProvider.now(request);

        if (paste.getExpiresAtMs() != null && now >= paste.getExpiresAtMs()) {
            redis.delete(key);
            return notFound();
        }

        if (paste.getRemainingViews() != null) {
            if (paste.getRemainingViews() <= 0) {
                redis.delete(key);
                return notFound();
            }
            paste.setRemainingViews(paste.getRemainingViews() - 1);
            redis.opsForValue().set(key,
                    mapper.writeValueAsString(paste));
        }

        return ResponseEntity.ok(Map.of(
                "content", paste.getContent(),
                "remaining_views", paste.getRemainingViews(),
                "expires_at", paste.getExpiresAtMs() == null
                        ? null
                        : Instant.ofEpochMilli(paste.getExpiresAtMs()).toString()
        ));
    }

    private ResponseEntity<Map<String, String>> bad() {
        return ResponseEntity.badRequest()
                .body(Map.of("error", "Invalid input"));
    }

    private ResponseEntity<Map<String, String>> notFound() {
        return ResponseEntity.status(404)
                .body(Map.of("error", "Not found"));
    }
}

