package com.casrusil.SII_ERP_AI.modules.ai_assistant.infrastructure.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for embeddings and RAG support.
 * Uses all-MiniLM-L6-v2 model for generating embeddings.
 * In-memory store for MVP (can be replaced with persistent store later).
 */
@Configuration
public class EmbeddingConfig {

    /**
     * Embedding model for converting text to vectors.
     * all-MiniLM-L6-v2: 384-dimensional embeddings, works well for Spanish.
     */
    @Bean
    public EmbeddingModel embeddingModel() {
        return new AllMiniLmL6V2EmbeddingModel();
    }

    /**
     * In-memory embedding store for MVP.
     * For production, replace with persistent store (PgVector, Pinecone, etc.)
     */
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }
}
