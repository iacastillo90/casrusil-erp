package com.casrusil.siierpai;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan("com.casrusil.siierpai")
@EnableJpaRepositories("com.casrusil.siierpai")
public class PersistenceConfig {
}
