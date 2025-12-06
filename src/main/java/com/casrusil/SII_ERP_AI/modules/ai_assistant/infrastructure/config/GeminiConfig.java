package com.casrusil.SII_ERP_AI.modules.ai_assistant.infrastructure.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración para la integración con Google Gemini AI.
 * 
 * <p>
 * Configura el bean {@link ChatLanguageModel} usando LangChain4j.
 * Se utiliza una temperatura de 0.0 para garantizar respuestas deterministas,
 * crucial para aplicaciones contables y financieras donde la precisión es
 * prioritaria
 * sobre la creatividad.
 * 
 * <h2>Propiedades requeridas:</h2>
 * <ul>
 * <li>{@code langchain4j.google-ai-gemini.chat-model.api-key}</li>
 * <li>{@code langchain4j.google-ai-gemini.chat-model.model-name} (default:
 * gemini-1.5-flash)</li>
 * </ul>
 * 
 * @since 1.0
 */
@Configuration
public class GeminiConfig {

    @Value("${langchain4j.google-ai-gemini.chat-model.api-key}")
    private String apiKey;

    @Value("${langchain4j.google-ai-gemini.chat-model.model-name:gemini-1.5-flash}")
    private String modelName;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(0.0) // Deterministic for accounting
                .build();
    }
}
