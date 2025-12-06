package com.casrusil.SII_ERP_AI.modules.ai_assistant.domain.model;

import java.time.Instant;

public record Message(
        String role, // user, assistant, system
        String content,
        Instant timestamp) {
    public Message {
        if (timestamp == null)
            timestamp = Instant.now();
    }

    public static Message user(String content) {
        return new Message("user", content, Instant.now());
    }

    public static Message assistant(String content) {
        return new Message("assistant", content, Instant.now());
    }

    public static Message system(String content) {
        return new Message("system", content, Instant.now());
    }
}
