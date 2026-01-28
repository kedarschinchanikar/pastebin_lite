package com.example.pastebin.controller;

import com.example.pastebin.model.Paste;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.time.Duration;
import java.util.UUID;

@Controller
public class UiController {

    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;

    public UiController(StringRedisTemplate redis, ObjectMapper mapper) {
        this.redis = redis;
        this.mapper = mapper;
    }

    @GetMapping("/")
    @ResponseBody
    public String home() {
        return """
        <html>
        <body style="font-family:Arial;background:#f4f6f8;padding:40px">
          <div style="background:#fff;padding:20px;width:420px;margin:auto">
            <h2>Create Paste</h2>
            <form method="post" action="/create">
              <textarea name="content" rows="6" style="width:100%"></textarea>
              <input name="ttl" type="number" placeholder="TTL seconds" style="width:100%">
              <input name="views" type="number" placeholder="Max views" style="width:100%">
              <button>Create</button>
            </form>
          </div>
        </body>
        </html>
        """;
    }

    @PostMapping("/create")
    @ResponseBody
    public String create(@RequestParam String content,
                         @RequestParam(required = false) Integer ttl,
                         @RequestParam(required = false) Integer views)
            throws Exception {

        if (content.trim().isEmpty())
            return "<h3>Error: Content cannot be empty</h3>";

        String id = UUID.randomUUID().toString().substring(0, 8);
        Paste paste = new Paste(
                content,
                ttl == null ? null : System.currentTimeMillis() + ttl * 1000L,
                views
        );

        redis.opsForValue().set(
                "paste:" + id,
                mapper.writeValueAsString(paste)
        );

        if (ttl != null)
            redis.expire("paste:" + id, Duration.ofSeconds(ttl));

        return "<a href='/p/" + id + "'>View Paste</a>";
    }

    @GetMapping("/p/{id}")
    @ResponseBody
    public ResponseEntity<String> view(@PathVariable String id)
            throws Exception {

        String json = redis.opsForValue().get("paste:" + id);
        if (json == null)
            return ResponseEntity.status(404).body("Not Found");

        Paste paste = mapper.readValue(json, Paste.class);

        return ResponseEntity.ok(
                "<pre>" +
                        HtmlUtils.htmlEscape(paste.getContent()) +
                        "</pre>"
        );
    }
}