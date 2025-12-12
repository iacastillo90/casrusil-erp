package com.casrusil.siierpai.shared.domain.event;

import java.time.Instant;

/**
 * Interfaz marcador para todos los eventos del dominio.
 * 
 * <p>
 * Un evento de dominio representa algo que sucedió en el pasado y es relevante
 * para el negocio.
 * Todos los eventos deben ser inmutables y contener una marca de tiempo de
 * cuándo ocurrieron.
 * 
 * <h2>Características:</h2>
 * <ul>
 * <li>Inmutabilidad: Los eventos no cambian una vez creados.</li>
 * <li>Nombre en pasado: Representan hechos consumados (ej:
 * InvoiceCreated).</li>
 * <li>Relevancia: Solo eventos importantes para el negocio.</li>
 * </ul>
 * 
 * @see EventPublisher
 * @since 1.0
 */
public interface DomainEvent {
    /**
     * @return El momento exacto en que ocurrió el evento.
     */
    Instant occurredOn();
}
