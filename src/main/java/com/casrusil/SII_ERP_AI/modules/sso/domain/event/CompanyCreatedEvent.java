package com.casrusil.SII_ERP_AI.modules.sso.domain.event;

import com.casrusil.SII_ERP_AI.modules.sso.domain.model.Company;
import com.casrusil.SII_ERP_AI.shared.domain.event.DomainEvent;

import java.time.Instant;

/**
 * Evento de dominio publicado cuando se crea una nueva empresa.
 * 
 * <p>
 * Este evento dispara la creación automática del plan de cuentas contable
 * para la nueva empresa mediante
 * {@link com.casrusil.SII_ERP_AI.modules.accounting.application.listener.CompanyCreatedListener}.
 * 
 * <h2>Flujo de eventos:</h2>
 * <ol>
 * <li>Usuario registra empresa vía
 * {@link com.casrusil.SII_ERP_AI.modules.sso.domain.port.in.RegisterCompanyUseCase}</li>
 * <li>Se crea entidad {@link Company}</li>
 * <li>Se publica este evento</li>
 * <li>Listener crea plan de cuentas automáticamente</li>
 * </ol>
 * 
 * @param company    La empresa recién creada
 * @param occurredOn Timestamp de cuándo ocurrió el evento
 * @see Company
 * @see com.casrusil.SII_ERP_AI.modules.accounting.application.listener.CompanyCreatedListener
 * @since 1.0
 */
public record CompanyCreatedEvent(Company company, Instant occurredOn) implements DomainEvent {
    public CompanyCreatedEvent(Company company) {
        this(company, Instant.now());
    }
}
