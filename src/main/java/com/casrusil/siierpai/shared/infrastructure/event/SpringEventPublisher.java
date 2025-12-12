package com.casrusil.siierpai.shared.infrastructure.event;

import com.casrusil.siierpai.shared.domain.event.DomainEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.casrusil.siierpai.shared.domain.event.EventPublisher;

/**
 * Adaptador de infraestructura que implementa {@link EventPublisher} usando
 * Spring Events.
 * 
 * <p>
 * Permite que el dominio publique eventos sin depender directamente de Spring.
 * Implementa el patrón Adapter para desacoplar el dominio de la
 * infraestructura.
 * 
 * <h2>Responsabilidades:</h2>
 * <ul>
 * <li>Adaptar {@link EventPublisher} del dominio a Spring
 * {@link ApplicationEventPublisher}</li>
 * <li>Publicar eventos de dominio de forma asíncrona</li>
 * <li>Mantener el dominio libre de dependencias de Spring</li>
 * </ul>
 * 
 * <h2>Flujo de eventos:</h2>
 * <ol>
 * <li>Dominio publica evento vía {@link EventPublisher}</li>
 * <li>Este adapter delega a Spring {@link ApplicationEventPublisher}</li>
 * <li>Spring distribuye el evento a todos los {@code @EventListener}</li>
 * <li>Listeners procesan el evento de forma asíncrona</li>
 * </ol>
 * 
 * @see EventPublisher
 * @see DomainEvent
 * @since 1.0
 */
@Component
public class SpringEventPublisher implements EventPublisher {

    private final ApplicationEventPublisher publisher;

    public SpringEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void publish(DomainEvent event) {
        publisher.publishEvent(event);
    }
}
