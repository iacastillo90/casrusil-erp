package com.casrusil.siierpai.modules.integration_sii.domain.event;

import com.casrusil.siierpai.modules.integration_sii.domain.model.RcvData;
import com.casrusil.siierpai.shared.domain.event.DomainEvent;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

import java.time.Instant;
import java.util.List;

/**
 * Evento de dominio que indica que se han descargado nuevos registros de
 * compra/venta (RCV) del SII.
 * 
 * <p>
 * Este evento contiene la información cruda obtenida del Registro de Compras y
 * Ventas del SII.
 * Es el paso previo a la conciliación y generación de propuestas de F29.
 * 
 * <h2>Propósito:</h2>
 * <ul>
 * <li>Notificar la disponibilidad de nuevos datos fiscales.</li>
 * <li>Iniciar el proceso de conciliación bancaria vs SII.</li>
 * <li>Alimentar el asistente de IA con datos actualizados.</li>
 * </ul>
 * 
 * @param companyId   ID de la empresa.
 * @param rcvDataList Lista de registros obtenidos.
 * @param occurredOn  Momento de la descarga.
 * 
 * @see com.casrusil.siierpai.modules.integration_sii.domain.model.RcvData
 * @since 1.0
 */
public record RcvDownloadedEvent(
        CompanyId companyId,
        List<RcvData> rcvDataList,
        Instant occurredOn) implements DomainEvent {

    /**
     * Constructor de conveniencia.
     * 
     * @param companyId   ID de la empresa.
     * @param rcvDataList Lista de registros.
     */
    public RcvDownloadedEvent(CompanyId companyId, List<RcvData> rcvDataList) {
        this(companyId, rcvDataList, Instant.now());
    }
}
