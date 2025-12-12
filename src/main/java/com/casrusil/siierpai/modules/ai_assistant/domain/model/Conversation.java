package com.casrusil.siierpai.modules.ai_assistant.domain.model;

import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.domain.valueobject.UserId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Conversation {
    private final UUID id;
    private final CompanyId companyId;
    private final UserId userId;
    private final Instant startedAt;
    private final List<Message> messages;

    public Conversation(CompanyId companyId, UserId userId) {
        this.id = UUID.randomUUID();
        this.companyId = companyId;
        this.userId = userId;
        this.startedAt = Instant.now();
        this.messages = new ArrayList<>();
    }

    public void addMessage(Message message) {
        this.messages.add(message);
    }

    public UUID getId() {
        return id;
    }

    public CompanyId getCompanyId() {
        return companyId;
    }

    public UserId getUserId() {
        return userId;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public List<Message> getMessages() {
        return Collections.unmodifiableList(messages);
    }
}
