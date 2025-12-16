package com.casrusil.siierpai.modules.financial_shield.domain.model;

public record RiskFactor(
        String description,
        Severity severity) {
    public enum Severity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}
