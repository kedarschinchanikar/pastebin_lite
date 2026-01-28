package com.example.pastebin.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class jacksonconfig {
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
