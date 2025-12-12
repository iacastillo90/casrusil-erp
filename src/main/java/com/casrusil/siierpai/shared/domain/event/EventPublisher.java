package com.casrusil.siierpai.shared.domain.event;

/**
 * Puerto de salida para publicar eventos de dominio.
 * 
 * <p>
 * Esta interfaz permite al dominio notificar al resto del sistema que algo ha
 * sucedido,
 * sin acoplarse a mecanismos específicos de mensajería (como Spring Events,
 * Kafka, etc.).
 * 
 * <h2>Uso:</h2>
 * 
 * <pre>{@code
 * // En un caso de uso o servicio de dominio
 * public void createInvoice(Invoice invoice) {
 *     repository.save(invoice);
 *     eventPublisher.publish(new InvoiceCreatedEvent(invoice));
 * }
 * }</pre>
 * 
 * @see DomainEvent
 * @since 1.0
 */
public interface EventPublisher {
    /**
     * Publica un evento de dominio para que sea procesado por los listeners
     * interesados.
     * 
     * @param event El evento a publicar. No debe ser null.
     */
    void publish(DomainEvent event);
}
