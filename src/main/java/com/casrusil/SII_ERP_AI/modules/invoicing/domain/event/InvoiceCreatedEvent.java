package com.casrusil.SII_ERP_AI.modules.invoicing.domain.event;

import com.casrusil.SII_ERP_AI.modules.invoicing.domain.model.Invoice;
import com.casrusil.SII_ERP_AI.shared.domain.event.DomainEvent;
import java.time.Instant;

/**
 * Evento de dominio publicado cuando se crea una nueva factura (DTE).
 * 
 * <p>
 * Este evento dispara la generación automática de asientos contables
 * mediante
 * {@link com.casrusil.SII_ERP_AI.modules.accounting.application.listener.InvoiceAccountingListener}.
 * 
 * <h2>Flujo de eventos:</h2>
 * <ol>
 * <li>Factura creada (manual o sincronización SII)</li>
 * <li>Se publica este evento</li>
 * <li>Listener genera asiento contable automáticamente</li>
 * <li>Se aplican reglas de clasificación aprendidas</li>
 * </ol>
 * 
 * @param invoice    La factura recién creada
 * @param occurredOn Timestamp de cuándo ocurrió el evento
 * @see com.casrusil.SII_ERP_AI.modules.invoicing.domain.model.Invoice
 * @see com.casrusil.SII_ERP_AI.modules.accounting.application.listener.InvoiceAccountingListener
 * @since 1.0
 */
public record InvoiceCreatedEvent(Invoice invoice, Instant occurredOn) implements DomainEvent {
    public InvoiceCreatedEvent(Invoice invoice) {
        this(invoice, Instant.now());
    }
}
