package com.casrusil.SII_ERP_AI.shared.infrastructure.config;

import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;

import java.util.concurrent.Executors;

/**
 * Configuración de concurrencia usando Virtual Threads (Java 21+).
 * 
 * <p>
 * Reemplaza el ejecutor de tareas por defecto de Spring Boot con uno basado
 * en Virtual Threads. Esto permite manejar miles de conexiones concurrentes
 * (ej: múltiples usuarios chateando con la IA o descargas masivas del SII)
 * con un consumo de memoria muy bajo y sin bloquear hilos del sistema
 * operativo.
 * 
 * <h2>Beneficios:</h2>
 * <ul>
 * <li>Alta escalabilidad para operaciones I/O bound.</li>
 * <li>Mejor utilización de recursos.</li>
 * <li>Elimina la necesidad de pools de hilos complejos.</li>
 * </ul>
 * 
 * @since 1.0
 */
@Configuration
public class VirtualThreadConfig {

    @Bean(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)
    public AsyncTaskExecutor applicationTaskExecutor() {
        return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
    }
}
