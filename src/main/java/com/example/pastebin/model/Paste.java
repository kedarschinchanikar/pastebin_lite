package com.example.pastebin.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Paste {
    private String content;
    private Long expiresAtMs;
    private Integer remainingViews;
}
