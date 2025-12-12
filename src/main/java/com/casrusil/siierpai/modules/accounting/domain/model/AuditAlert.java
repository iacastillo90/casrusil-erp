package com.casrusil.siierpai.modules.accounting.domain.model;

import java.util.UUID;

/**
 * Representa una alerta de auditoría detectada por el sistema.
 */
public class AuditAlert {

    public enum Severity {
        INFO,
        WARNING,
        CRITICAL
    }

    public enum Type {
        DUPLICATE_INVOICE,
        SUSPICIOUS_AMOUNT,
        INVALID_ACCOUNT,
        UNBALANCED_ENTRY,
        MISSING_DOCUMENTATION
    }

    private final UUID id;
    private final Type type;
    private final Severity severity;
    private final String title;
    private final String description;
    private final String affectedEntityId;
    private final String suggestedAction;

    public AuditAlert(Type type, Severity severity, String title, String description,
            String affectedEntityId, String suggestedAction) {
        this.id = UUID.randomUUID();
        this.type = type;
        this.severity = severity;
        this.title = title;
        this.description = description;
        this.affectedEntityId = affectedEntityId;
        this.suggestedAction = suggestedAction;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getAffectedEntityId() {
        return affectedEntityId;
    }

    public String getSuggestedAction() {
        return suggestedAction;
    }
}
