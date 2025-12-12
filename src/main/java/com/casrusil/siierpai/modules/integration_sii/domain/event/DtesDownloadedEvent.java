package com.casrusil.siierpai.modules.integration_sii.domain.event;

import com.casrusil.siierpai.modules.integration_sii.domain.model.RcvData;
import com.casrusil.siierpai.shared.domain.event.DomainEvent;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

import java.time.Instant;
import java.util.List;

/**
 * Evento de dominio que indica que se han descargado nuevos DTEs (Documentos
 * Tributarios Electrónicos) del SII.
 * 
 * <p>
 * Este evento se dispara después de una sincronización exitosa con el SII,
 * conteniendo
 * la lista de documentos recibidos (RCV) que deben ser procesados por el
 * sistema.
 * 
 * <h2>Propósito:</h2>
 * <ul>
 * <li>Notificar que hay nuevos documentos disponibles.</li>
 * <li>Desencadenar la creación automática de facturas en el sistema.</li>
 * <li>Iniciar procesos de auditoría o clasificación automática.</li>
 * </ul>
 * 
 * @param companyId   ID de la empresa a la que pertenecen los documentos.
 * @param rcvDataList Lista de datos crudos (RCV) descargados del SII.
 * @param occurredOn  Momento en que ocurrió la descarga.
 * 
 * @see com.casrusil.siierpai.modules.integration_sii.domain.model.RcvData
 * @since 1.0
 */
public record DtesDownloadedEvent(
        CompanyId companyId,
        List<RcvData> rcvDataList,
        Instant occurredOn) implements DomainEvent {

    /**
     * Constructor de conveniencia que asigna automáticamente la fecha actual.
     * 
     * @param companyId   ID de la empresa.
     * @param rcvDataList Lista de documentos descargados.
     */
    public DtesDownloadedEvent(CompanyId companyId, List<RcvData> rcvDataList) {
        this(companyId, rcvDataList, Instant.now());
    }
}
