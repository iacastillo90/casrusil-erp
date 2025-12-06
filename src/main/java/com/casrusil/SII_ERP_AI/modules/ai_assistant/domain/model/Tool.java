package com.casrusil.SII_ERP_AI.modules.ai_assistant.domain.model;

public interface Tool {
    String name();

    String description();

    String execute(String arguments);
}
