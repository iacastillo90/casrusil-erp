package com.casrusil.siierpai.shared.infrastructure.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class SchemaInitializer {

    @Bean
    public InitializingBean initSchemas(DataSource dataSource) {
        return () -> {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS accounting");
            jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS invoicing");
            jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS sso");
        };
    }
}
