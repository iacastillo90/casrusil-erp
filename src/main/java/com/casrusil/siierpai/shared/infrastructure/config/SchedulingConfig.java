package com.casrusil.siierpai.shared.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration for Spring Scheduling.
 * Enables @Scheduled annotations for background tasks.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
