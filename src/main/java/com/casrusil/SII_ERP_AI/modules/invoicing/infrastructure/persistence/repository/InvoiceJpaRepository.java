package com.casrusil.SII_ERP_AI.modules.invoicing.infrastructure.persistence.repository;

import com.casrusil.SII_ERP_AI.modules.invoicing.infrastructure.persistence.entity.InvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio JPA para facturas.
 * 
 * <p>
 * Maneja el almacenamiento y recuperación de documentos tributarios.
 * Incluye consultas para búsqueda por folio, RUT emisor/receptor y fecha.
 * 
 * @since 1.0
 */
@Repository
public interface InvoiceJpaRepository extends JpaRepository<InvoiceEntity, UUID> {
    List<InvoiceEntity> findAllByCompanyId(UUID companyId);

    boolean existsByCompanyIdAndFolioAndIssuerRut(UUID companyId, Long folio, String issuerRut);
}
