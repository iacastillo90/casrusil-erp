package com.casrusil.SII_ERP_AI.modules.ai_assistant.infrastructure.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PgVectorConfig {

    @Value("${langchain4j.pgvector.host}")
    private String host;

    @Value("${langchain4j.pgvector.port}")
    private int port;

    @Value("${langchain4j.pgvector.user}")
    private String user;

    @Value("${langchain4j.pgvector.password}")
    private String password;

    @Value("${langchain4j.pgvector.database}")
    private String database;

    @Value("${langchain4j.pgvector.table}")
    private String table;

    @Value("${langchain4j.pgvector.dimension}")
    private int dimension;

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return PgVectorEmbeddingStore.builder()
                .host(host)
                .port(port)
                .user(user)
                .password(password)
                .database(database)
                .table(table)
                .dimension(dimension)
                .build();
    }
}
